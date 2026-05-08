package com.rudra.internetspeedtest.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.rudra.internetspeedtest.domain.model.BufferbloatGrade
import com.rudra.internetspeedtest.domain.model.SpeedTestProgress
import com.rudra.internetspeedtest.domain.model.SpeedTestResult
import com.rudra.internetspeedtest.domain.model.TestPhase
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
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.random.Random

@Singleton
class AccurateSpeedTestRepositoryImpl @Inject constructor(
    private val context: Context
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
        try {
            val connectionInfo = detectConnectionType()

            onProgress(
                SpeedTestProgress(
                    phase = TestPhase.PING,
                    progress = 0f,
                    message = "Testing idle ping...",
                    connectionType = connectionInfo.first,
                    ispName = connectionInfo.second
                )
            )

            val idlePings = mutableListOf<Double>()
            repeat(10) { i ->
                val ping = measurePing("https://www.google.com")
                if (ping > 0) idlePings.add(ping)
                onProgress(
                    SpeedTestProgress(
                        phase = TestPhase.PING,
                        progress = (i + 1) / 10f * 0.15f,
                        pingMs = if (idlePings.isNotEmpty()) idlePings.average() else 0.0,
                        connectionType = connectionInfo.first,
                        ispName = connectionInfo.second,
                        message = "Idle ping ${i + 1}/10..."
                    )
                )
            }

            val idlePing = if (idlePings.isNotEmpty()) idlePings.average() else 0.0
            val pingJitter = calculateJitter(idlePings)

            onProgress(
                SpeedTestProgress(
                    phase = TestPhase.DOWNLOAD,
                    progress = 0.15f,
                    pingMs = idlePing,
                    jitterMs = pingJitter,
                    message = "Testing download speed...",
                    connectionType = connectionInfo.first,
                    ispName = connectionInfo.second
                )
            )

            val result1 = runDownloadWithLoadedPing(
                url = downloadUrls.random(),
                threadCount = 4
            )

            val downloadSpeed = result1.first
            val downloadSamples = result1.second
            val loadedDownloadPings = result1.third

            repeat(5) { sample ->
                onProgress(
                    SpeedTestProgress(
                        phase = TestPhase.DOWNLOAD,
                        progress = 0.15f + (sample.toFloat() / 5) * 0.45f,
                        currentSpeed = downloadSpeed,
                        pingMs = idlePing,
                        loadedPingMs = if (loadedDownloadPings.isNotEmpty()) loadedDownloadPings.average() else 0.0,
                        jitterMs = pingJitter,
                        speedSamples = downloadSamples,
                        message = "Download: ${String.format("%.1f", downloadSpeed)} Mbps",
                        connectionType = connectionInfo.first,
                        ispName = connectionInfo.second
                    )
                )
                delay(100)
            }

            val loadedDownloadPing = if (loadedDownloadPings.isNotEmpty()) loadedDownloadPings.average() else 0.0

            onProgress(
                SpeedTestProgress(
                    phase = TestPhase.UPLOAD,
                    progress = 0.6f,
                    pingMs = idlePing,
                    loadedPingMs = loadedDownloadPing,
                    jitterMs = pingJitter,
                    message = "Testing upload speed..."
                )
            )

            val result2 = runUploadWithLoadedPing(threadCount = 2)

            val uploadSpeed = result2.first
            val uploadSamples = result2.second
            val loadedUploadPings = result2.third

            repeat(5) { sample ->
                onProgress(
                    SpeedTestProgress(
                        phase = TestPhase.UPLOAD,
                        progress = 0.6f + (sample.toFloat() / 5) * 0.3f,
                        currentSpeed = uploadSpeed,
                        pingMs = idlePing,
                        loadedPingMs = if (loadedUploadPings.isNotEmpty()) loadedUploadPings.average() else 0.0,
                        jitterMs = pingJitter,
                        speedSamples = uploadSamples,
                        message = "Upload: ${String.format("%.1f", uploadSpeed)} Mbps",
                        connectionType = connectionInfo.first,
                        ispName = connectionInfo.second
                    )
                )
                delay(100)
            }

            val loadedUploadPing = if (loadedUploadPings.isNotEmpty()) loadedUploadPings.average() else 0.0

            val finalBufferbloatGrade = calculateBufferbloatGrade(idlePing, loadedDownloadPing, loadedUploadPing)

            val confidenceScore = calculateConfidenceScore(
                downloadSamples + uploadSamples,
                idlePing,
                loadedDownloadPing,
                loadedUploadPing
            )

            onProgress(
                SpeedTestProgress(
                    phase = TestPhase.COMPLETE,
                    progress = 1f,
                    currentSpeed = downloadSpeed,
                    pingMs = idlePing,
                    loadedPingMs = max(loadedDownloadPing, loadedUploadPing),
                    jitterMs = pingJitter,
                    bufferbloatGrade = finalBufferbloatGrade,
                    confidenceScore = confidenceScore,
                    speedSamples = downloadSamples + uploadSamples,
                    message = "Test complete! Bufferbloat Grade: $finalBufferbloatGrade",
                    connectionType = connectionInfo.first,
                    ispName = connectionInfo.second
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
                confidenceScore = confidenceScore,
                connectionType = connectionInfo.first,
                ispName = connectionInfo.second,
                isCgnat = isCgnat(),
                testServer = "Multiple",
                status = TestStatus.SUCCESS,
                rawThreadSpeeds = downloadSamples + uploadSamples,
                speedSamples = downloadSamples + uploadSamples
            )

        } catch (e: Exception) {
            onProgress(
                SpeedTestProgress(
                    phase = TestPhase.FAILED,
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
                    synchronized(samples) {
                        samples.add(speed)
                    }
                    speed
                } catch (e: Exception) { 0.0 }
            }
        }

        val pingDeferred = async {
            repeat(20) {
                delay(100)
                val ping = measurePing("https://www.google.com")
                if (ping > 0) {
                    synchronized(loadedPings) {
                        loadedPings.add(ping)
                    }
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
                    synchronized(samples) {
                        samples.add(speed)
                    }
                    speed
                } catch (e: Exception) { 0.0 }
            }
        }

        val pingDeferred = async {
            repeat(15) {
                delay(100)
                val ping = measurePing("https://www.google.com")
                if (ping > 0) {
                    synchronized(loadedPings) {
                        loadedPings.add(ping)
                    }
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

    private fun calculateBufferbloatGrade(idlePing: Double, loadedDownloadPing: Double, loadedUploadPing: Double): String {
        val maxLoaded = max(loadedDownloadPing, loadedUploadPing)
        if (idlePing <= 0 || maxLoaded <= 0) return "N/A"
        return BufferbloatGrade.calculate(idlePing, maxLoaded)
    }

    private fun calculateConfidenceScore(
        samples: List<Double>,
        idlePing: Double,
        loadedDownloadPing: Double,
        loadedUploadPing: Double
    ): Int {
        var score = 100

        if (samples.isEmpty()) return 0

        val avg = samples.average()
        val variance = samples.map { (it - avg) * (it - avg) }.average()
        val stdDev = sqrt(variance)
        val coefficientOfVariation = if (avg > 0) (stdDev / avg) else 1.0

        score -= (coefficientOfVariation * 100).toInt().coerceIn(0, 40)

        val maxLoadedPing = max(loadedDownloadPing, loadedUploadPing)
        if (idlePing > 0) {
            val bufferbloatIncrease = maxLoadedPing - idlePing
            score -= when {
                bufferbloatIncrease > 500 -> 30
                bufferbloatIncrease > 300 -> 20
                bufferbloatIncrease > 100 -> 10
                else -> 0
            }
        }

        return score.coerceIn(0, 100)
    }

    private fun detectConnectionType(): Pair<String, String> {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork
            val capabilities = cm.getNetworkCapabilities(network)

            val type = when {
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "WiFi"
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Mobile Data"
                else -> "Unknown"
            }

            val isp = getIspName()

            Pair(type, isp)
        } catch (e: Exception) {
            Pair("Unknown", "Unknown")
        }
    }

    private fun getIspName(): String {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (intf in interfaces) {
                for (addr in intf.inetAddresses) {
                    if (!addr.isLoopbackAddress && addr is InetAddress) {
                        val hostName = addr.hostName
                        if (hostName.contains(".")) {
                            return hostName.substringAfterLast(".", "Unknown")
                        }
                    }
                }
            }
            "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun isCgnat(): Boolean {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (intf in interfaces) {
                for (addr in intf.inetAddresses) {
                    if (!addr.isLoopbackAddress) {
                        val ip = addr.hostAddress ?: continue
                        if (ip.contains(":")) continue
                        val parts = ip.split(".")
                        if (parts.size == 4) {
                            val firstOctet = parts[0].toIntOrNull() ?: continue
                            if (firstOctet in 100..10) {
                                return true
                            }
                        }
                    }
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }
}
