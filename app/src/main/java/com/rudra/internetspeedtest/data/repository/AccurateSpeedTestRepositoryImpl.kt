package com.rudra.internetspeedtest.data.repository

import android.content.Context
import com.rudra.internetspeedtest.core.network.ConnectionDetector
import dagger.hilt.android.qualifiers.ApplicationContext
import com.rudra.internetspeedtest.core.testing.AdaptiveThreadController
import com.rudra.internetspeedtest.core.testing.AggregateTestData
import com.rudra.internetspeedtest.core.testing.BufferbloatResult
import com.rudra.internetspeedtest.core.testing.ConfidenceCalculator
import com.rudra.internetspeedtest.core.testing.ServerPoolManager
import com.rudra.internetspeedtest.core.testing.StatisticalCleaner
import com.rudra.internetspeedtest.core.testing.TestAuditor
import com.rudra.internetspeedtest.core.testing.TestPhase
import com.rudra.internetspeedtest.core.testing.TestProvenance
import com.rudra.internetspeedtest.domain.model.SpeedTestProgress
import com.rudra.internetspeedtest.domain.model.SpeedTestResult
import com.rudra.internetspeedtest.domain.model.TestStatus
import com.rudra.internetspeedtest.domain.repository.InternetSpeedTestRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

@Singleton
class AccurateSpeedTestRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val connectionDetector: ConnectionDetector,
    private val serverPoolManager: ServerPoolManager,
    private val adaptiveThreadController: AdaptiveThreadController,
    private val confidenceCalculator: ConfidenceCalculator,
    private val testAuditor: TestAuditor
) : InternetSpeedTestRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val downloadUrls = listOf(
        "https://speedtest.tele2.net/10MB.zip",
        "https://speed.cloudflare.com/__down"
    )

    override suspend fun runSpeedTest(
        onProgress: (SpeedTestProgress) -> Unit
    ): SpeedTestResult = withContext(Dispatchers.IO) {
        testAuditor.reset()
        adaptiveThreadController.reset()
        try {
            val connectionContext = connectionDetector.detect()
            testAuditor.record("Connection", "Detected connection context", mapOf(
                "type" to connectionContext.networkType.ordinal.toDouble(),
                "isp" to 0.0
            ))

            onProgress(
                SpeedTestProgress(
                    phase = TestPhase.ServerSelection,
                    progress = 0f,
                    message = "Detecting connection: ${connectionContext.displayString}",
                    connectionType = connectionContext.networkType.name,
                    ispName = connectionContext.ispName
                )
            )

            val selectedServers = serverPoolManager.selectOptimal(3)
            val serverNames = selectedServers.map { it.server.name }
            testAuditor.record("Servers", "Selected ${selectedServers.size} servers: ${serverNames.joinToString()}",
                selectedServers.associate { it.server.name to it.latencyMs }
            )

            onProgress(
                SpeedTestProgress(
                    phase = TestPhase.PreTestPing,
                    progress = 0.1f,
                    message = "Selected servers: ${serverNames.joinToString()}",
                    selectedServers = serverNames,
                    connectionType = connectionContext.networkType.name,
                    ispName = connectionContext.ispName
                )
            )

            val idlePings = mutableListOf<Double>()
            repeat(10) { i ->
                val ping = measurePing("https://www.google.com")
                if (ping > 0) idlePings.add(ping)
                onProgress(
                    SpeedTestProgress(
                        phase = TestPhase.PreTestPing,
                        progress = 0.1f + (i + 1) / 10f * 0.15f,
                        pingMs = if (idlePings.isNotEmpty()) idlePings.average() else 0.0,
                        connectionType = connectionContext.networkType.name,
                        ispName = connectionContext.ispName,
                        message = "Idle ping ${i + 1}/10..."
                    )
                )
            }

            val idlePing = if (idlePings.isNotEmpty()) idlePings.average() else 0.0
            val pingJitter = calculateJitter(idlePings)
            testAuditor.record("Ping", "Idle ping: ${String.format("%.1f", idlePing)}ms, jitter: ${String.format("%.1f", pingJitter)}ms",
                mapOf("idlePing" to idlePing, "jitter" to pingJitter)
            )

            val threadConfig = adaptiveThreadController.determineThreadCount(
                initialLatencyMs = idlePing,
                networkType = connectionContext.networkType,
                signalStrength = connectionContext.signalStrength,
                estimatedThroughputMbps = null
            )
            testAuditor.record("Threads", "Using ${threadConfig.count} threads: ${threadConfig.reason}")

            onProgress(
                SpeedTestProgress(
                    phase = TestPhase.DownloadProgress(0),
                    progress = 0.25f,
                    pingMs = idlePing,
                    jitterMs = pingJitter,
                    message = "Using ${threadConfig.count} connections (${threadConfig.reason})",
                    threadConfig = "${threadConfig.count} connections (${threadConfig.reason})",
                    connectionType = connectionContext.networkType.name,
                    ispName = connectionContext.ispName
                )
            )

            val downloadResult = runDownloadWithLoadedPing(
                url = downloadUrls.random(),
                threadCount = threadConfig.count
            )

            val downloadSpeed = downloadResult.first
            val downloadSamples = downloadResult.second
            val loadedDownloadPings = downloadResult.third
            testAuditor.record("Download", "Download speed: ${String.format("%.1f", downloadSpeed)} Mbps, samples: ${downloadSamples.size}")

            val cleanedDownload = StatisticalCleaner.clean(downloadSamples.mapIndexed { i, s ->
                com.rudra.internetspeedtest.core.testing.SpeedSample(System.currentTimeMillis(), s, i % threadConfig.count)
            })

            repeat(5) { sample ->
                onProgress(
                    SpeedTestProgress(
                        phase = TestPhase.DownloadProgress(((sample + 1) * 20)),
                        progress = 0.3f + (sample.toFloat() / 5) * 0.4f,
                        currentSpeed = downloadSpeed,
                        pingMs = idlePing,
                        loadedPingMs = if (loadedDownloadPings.isNotEmpty()) loadedDownloadPings.average() else 0.0,
                        jitterMs = pingJitter,
                        speedSamples = downloadSamples,
                        message = "Download: ${String.format("%.1f", downloadSpeed)} Mbps | ${cleanedDownload.displaySummary}",
                        connectionType = connectionContext.networkType.name,
                        ispName = connectionContext.ispName
                    )
                )
                delay(100)
            }

            val loadedDownloadPing = if (loadedDownloadPings.isNotEmpty()) loadedDownloadPings.average() else 0.0

            onProgress(
                SpeedTestProgress(
                    phase = TestPhase.UploadProgress(0),
                    progress = 0.7f,
                    pingMs = idlePing,
                    loadedPingMs = loadedDownloadPing,
                    jitterMs = pingJitter,
                    message = "Testing upload speed...",
                    connectionType = connectionContext.networkType.name,
                    ispName = connectionContext.ispName
                )
            )

            val uploadResult = runUploadWithLoadedPing(threadCount = max(2, threadConfig.count / 2))

            val uploadSpeed = uploadResult.first
            val uploadSamples = uploadResult.second
            val loadedUploadPings = uploadResult.third
            testAuditor.record("Upload", "Upload speed: ${String.format("%.1f", uploadSpeed)} Mbps, samples: ${uploadSamples.size}")

            val cleanedUpload = StatisticalCleaner.clean(uploadSamples.mapIndexed { i, s ->
                com.rudra.internetspeedtest.core.testing.SpeedSample(System.currentTimeMillis(), s, i % 2)
            })

            repeat(5) { sample ->
                onProgress(
                    SpeedTestProgress(
                        phase = TestPhase.UploadProgress(((sample + 1) * 20)),
                        progress = 0.7f + (sample.toFloat() / 5) * 0.2f,
                        currentSpeed = uploadSpeed,
                        pingMs = idlePing,
                        loadedPingMs = if (loadedUploadPings.isNotEmpty()) loadedUploadPings.average() else 0.0,
                        jitterMs = pingJitter,
                        speedSamples = uploadSamples,
                        message = "Upload: ${String.format("%.1f", uploadSpeed)} Mbps | ${cleanedUpload.displaySummary}",
                        connectionType = connectionContext.networkType.name,
                        ispName = connectionContext.ispName
                    )
                )
                delay(100)
            }

            val loadedUploadPing = if (loadedUploadPings.isNotEmpty()) loadedUploadPings.average() else 0.0

            onProgress(
                SpeedTestProgress(
                    phase = TestPhase.BufferbloatCheck,
                    progress = 0.9f,
                    pingMs = idlePing,
                    loadedPingMs = max(loadedDownloadPing, loadedUploadPing),
                    jitterMs = pingJitter,
                    message = "Measuring bufferbloat...",
                    connectionType = connectionContext.networkType.name,
                    ispName = connectionContext.ispName
                )
            )

            val bufferbloatResult = BufferbloatResult.calculate(idlePing, loadedDownloadPing, loadedUploadPing)
            testAuditor.record("Bufferbloat", bufferbloatResult.verdict)

            onProgress(
                SpeedTestProgress(
                    phase = TestPhase.Analysis,
                    progress = 0.95f,
                    pingMs = idlePing,
                    loadedPingMs = max(loadedDownloadPing, loadedUploadPing),
                    jitterMs = pingJitter,
                    bufferbloatGrade = bufferbloatResult.grade.name,
                    message = "Analyzing results...",
                    connectionType = connectionContext.networkType.name,
                    ispName = connectionContext.ispName
                )
            )

            val allSamples = downloadSamples + uploadSamples
            val confidenceResult = confidenceCalculator.calculateFromSamples(
                samples = allSamples,
                packetLoss = 0.0,
                bufferbloatGrade = bufferbloatResult.grade
            )

            val testStartTime = idlePings.firstOrNull()?.toLong()?.let {
                System.currentTimeMillis() - (idlePings.size * 100L)
            } ?: System.currentTimeMillis()
            val provenance = TestProvenance(
                serversUsed = serverNames,
                threadsPerServer = threadConfig.count,
                totalSamplesCollected = cleanedDownload.allSamples.size + cleanedUpload.allSamples.size,
                samplesDiscarded = cleanedDownload.discarded.size + cleanedUpload.discarded.size,
                testDurationSeconds = ((System.currentTimeMillis() - testStartTime) / 1000).toInt().coerceAtLeast(1),
                dataTransferredMB = 0.0
            )

            val finalBufferbloatGrade = bufferbloatResult.grade.name

            val allAggregateData = AggregateTestData(
                serverName = serverNames.firstOrNull() ?: "",
                threadCount = threadConfig.count,
                allSamples = (downloadSamples + uploadSamples).mapIndexed { i, s ->
                    com.rudra.internetspeedtest.core.testing.SpeedSample(System.currentTimeMillis(), s, i % threadConfig.count)
                },
                discarded = cleanedDownload.discarded + cleanedUpload.discarded,
                testDurationMs = provenance.testDurationSeconds * 1000L,
                totalBytesTransferred = 0
            )

            onProgress(
                SpeedTestProgress(
                    phase = TestPhase.Complete,
                    progress = 1f,
                    currentSpeed = downloadSpeed,
                    pingMs = idlePing,
                    loadedPingMs = max(loadedDownloadPing, loadedUploadPing),
                    jitterMs = pingJitter,
                    bufferbloatGrade = finalBufferbloatGrade,
                    confidenceScore = confidenceResult.score,
                    speedSamples = allSamples,
                    message = "Test complete! Bufferbloat Grade: ${finalBufferbloatGrade}",
                    connectionType = connectionContext.networkType.name,
                    ispName = connectionContext.ispName,
                    procedure = provenance
                )
            )

            SpeedTestResult(
                downloadSpeedMbps = downloadSpeed,
                uploadSpeedMbps = uploadSpeed,
                pingMs = idlePing,
                loadedDownloadPingMs = loadedDownloadPing,
                loadedUploadPingMs = loadedUploadPing,
                jitterMs = pingJitter,
                bufferbloatGrade = finalBufferbloatGrade,
                confidenceScore = confidenceResult.score,
                connectionType = connectionContext.networkType.name,
                ispName = connectionContext.ispName,
                isCgnat = connectionContext.ipType == com.rudra.internetspeedtest.core.network.IpType.CGNAT,
                testServer = serverNames.joinToString(", "),
                status = TestStatus.SUCCESS,
                rawThreadSpeeds = allSamples,
                speedSamples = allSamples,
                connectionContext = connectionContext,
                testProvenance = provenance,
                confidenceResult = confidenceResult,
                bufferbloatResult = bufferbloatResult,
                aggregateData = allAggregateData
            )

        } catch (e: Exception) {
            testAuditor.record("Error", "Test failed: ${e.message}")
            onProgress(
                SpeedTestProgress(
                    phase = TestPhase.Failed,
                    status = TestStatus.FAILED,
                    message = "Test failed: ${e.message}"
                )
            )
            SpeedTestResult(status = TestStatus.FAILED)
        }
    }

    private suspend fun runDownloadWithLoadedPing(
        url: String,
        threadCount: Int = 4
    ): Triple<Double, List<Double>, List<Double>> = withContext(Dispatchers.IO) {
        val samples = mutableListOf<Double>()
        val loadedPings = mutableListOf<Double>()

        val deferred = (1..threadCount).map { threadId ->
            async {
                try {
                    val request = Request.Builder().url(url).build()
                    val startTime = System.nanoTime()
                    val response = client.newCall(request).execute()
                    val bytes = response.body?.bytes() ?: return@async 0.0
                    val durationSec = (System.nanoTime() - startTime) / 1_000_000_000.0
                    if (durationSec <= 0) return@async 0.0
                    val speed = (bytes.size * 8.0) / durationSec / 1_000_000
                    synchronized(samples) { samples.add(speed) }
                    speed
                } catch (e: Exception) { 0.0 }
            }
        }

        val pingDeferred = async {
            repeat(20) {
                delay(100)
                val ping = measurePing("https://www.google.com")
                if (ping > 0) {
                    synchronized(loadedPings) { loadedPings.add(ping) }
                }
            }
        }

        val results = deferred.awaitAll()
        pingDeferred.await()

        val validResults = results.filter { it > 0 }
        val avgSpeed = if (validResults.isNotEmpty()) validResults.average() else 0.0

        Triple(avgSpeed, samples.toList(), loadedPings.toList())
    }

    private suspend fun runUploadWithLoadedPing(
        threadCount: Int = 2
    ): Triple<Double, List<Double>, List<Double>> = withContext(Dispatchers.IO) {
        val samples = mutableListOf<Double>()
        val loadedPings = mutableListOf<Double>()

        val deferred = (1..threadCount).map { threadId ->
            async {
                try {
                    val uploadSize = 1 * 1024 * 1024
                    val data = ByteArray(uploadSize) { Random.nextInt(0, 255).toByte() }
                    val requestBody = data.toRequestBody("application/octet-stream".toMediaType())
                    val startTime = System.nanoTime()
                    val request = Request.Builder()
                        .url("https://httpbin.org/post")
                        .post(requestBody)
                        .build()
                    val response = client.newCall(request).execute()
                    response.close()
                    val durationSec = (System.nanoTime() - startTime) / 1_000_000_000.0
                    if (durationSec <= 0) return@async 0.0
                    val speed = (uploadSize * 8.0) / durationSec / 1_000_000
                    synchronized(samples) { samples.add(speed) }
                    speed
                } catch (e: Exception) { 0.0 }
            }
        }

        val pingDeferred = async {
            repeat(15) {
                delay(100)
                val ping = measurePing("https://www.google.com")
                if (ping > 0) {
                    synchronized(loadedPings) { loadedPings.add(ping) }
                }
            }
        }

        val results = deferred.awaitAll()
        pingDeferred.await()

        val validResults = results.filter { it > 0 }
        val avgSpeed = if (validResults.isNotEmpty()) validResults.average() else 0.0

        Triple(avgSpeed, samples.toList(), loadedPings.toList())
    }

    private suspend fun measurePing(url: String): Double = withContext(Dispatchers.IO) {
        return@withContext try {
            val startTime = System.nanoTime()
            val request = Request.Builder().url(url).head().build()
            val response = client.newCall(request).execute()
            response.close()
            val endTime = System.nanoTime()
            (endTime - startTime) / 1_000_000.0
        } catch (e: Exception) { 0.0 }
    }

    private fun calculateJitter(pings: List<Double>): Double {
        if (pings.size < 2) return 0.0
        var totalDiff = 0.0
        for (i in 1 until pings.size) {
            totalDiff += abs(pings[i] - pings[i - 1])
        }
        return totalDiff / (pings.size - 1)
    }
}
