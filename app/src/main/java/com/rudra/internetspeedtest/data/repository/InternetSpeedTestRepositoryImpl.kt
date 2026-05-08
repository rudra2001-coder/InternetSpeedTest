package com.rudra.internetspeedtest.data.repository

import com.rudra.internetspeedtest.domain.model.SpeedTestProgress
import com.rudra.internetspeedtest.domain.model.SpeedTestResult
import com.rudra.internetspeedtest.domain.model.TestPhase
import com.rudra.internetspeedtest.domain.model.TestStatus
import com.rudra.internetspeedtest.domain.repository.InternetSpeedTestRepository
import kotlinx.coroutines.Dispatchers
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
import kotlin.random.Random

@Singleton
class InternetSpeedTestRepositoryImpl @Inject constructor() : InternetSpeedTestRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val testServers = listOf(
        "https://speedtest.tele2.net",
        "https://httpbin.org"
    )

    private val downloadUrls = listOf(
        "https://speedtest.tele2.net/10MB.zip",
        "https://speedtest.tele2.net/5MB.zip"
    )

    override suspend fun runSpeedTest(
        onProgress: (SpeedTestProgress) -> Unit
    ): SpeedTestResult = withContext(Dispatchers.IO) {
        try {
            var pingMs = 0.0
            var jitterMs = 0.0
            var downloadSpeed = 0.0
            var uploadSpeed = 0.0
            var packetLoss = 0.0

            val selectedServer = testServers.first()

            onProgress(
                SpeedTestProgress(
                    phase = TestPhase.PING,
                    progress = 0f,
                    message = "Testing ping to $selectedServer..."
                )
            )

            val pingResults = mutableListOf<Long>()
            var failedPings = 0

            for (i in 1..5) {
                onProgress(
                    SpeedTestProgress(
                        phase = TestPhase.PING,
                        progress = i / 5f * 0.3f,
                        message = "Ping test $i/5...",
                        pingMs = if (pingResults.isNotEmpty()) pingResults.average() else 0.0,
                        jitterMs = calculateJitter(pingResults)
                    )
                )

                try {
                    val ping = measurePing(selectedServer)
                    if (ping > 0) {
                        pingResults.add(ping)
                    } else {
                        failedPings++
                    }
                } catch (e: Exception) {
                    failedPings++
                }
                delay(100)
            }

            pingMs = if (pingResults.isNotEmpty()) pingResults.average() else 0.0
            jitterMs = calculateJitter(pingResults)
            packetLoss = (failedPings / 5.0) * 100

            onProgress(
                SpeedTestProgress(
                    phase = TestPhase.PING,
                    progress = 0.3f,
                    message = "Ping: ${String.format("%.1f", pingMs)}ms | Jitter: ${String.format("%.1f", jitterMs)}ms",
                    pingMs = pingMs,
                    jitterMs = jitterMs,
                    packetLoss = packetLoss,
                    status = TestStatus.SUCCESS
                )
            )

            delay(500)

            onProgress(
                SpeedTestProgress(
                    phase = TestPhase.DOWNLOAD,
                    progress = 0.3f,
                    message = "Starting download test...",
                    pingMs = pingMs,
                    jitterMs = jitterMs,
                    packetLoss = packetLoss
                )
            )

            downloadSpeed = runDownloadTest { progress, speed ->
                onProgress(
                    SpeedTestProgress(
                        phase = TestPhase.DOWNLOAD,
                        progress = 0.3f + (progress * 0.4f),
                        message = "Downloading... ${String.format("%.1f", speed)} Mbps",
                        currentSpeed = speed,
                        pingMs = pingMs,
                        jitterMs = jitterMs,
                        packetLoss = packetLoss
                    )
                )
            }

            onProgress(
                SpeedTestProgress(
                    phase = TestPhase.DOWNLOAD,
                    progress = 0.7f,
                    message = "Download: ${String.format("%.1f", downloadSpeed)} Mbps",
                    currentSpeed = downloadSpeed,
                    pingMs = pingMs,
                    jitterMs = jitterMs,
                    packetLoss = packetLoss,
                    status = TestStatus.SUCCESS
                )
            )

            delay(500)

            onProgress(
                SpeedTestProgress(
                    phase = TestPhase.UPLOAD,
                    progress = 0.7f,
                    message = "Starting upload test...",
                    currentSpeed = downloadSpeed,
                    pingMs = pingMs,
                    jitterMs = jitterMs,
                    packetLoss = packetLoss
                )
            )

            uploadSpeed = runUploadTest { progress, speed ->
                onProgress(
                    SpeedTestProgress(
                        phase = TestPhase.UPLOAD,
                        progress = 0.7f + (progress * 0.3f),
                        message = "Uploading... ${String.format("%.1f", speed)} Mbps",
                        currentSpeed = speed,
                        pingMs = pingMs,
                        jitterMs = jitterMs,
                        packetLoss = packetLoss
                    )
                )
            }

            onProgress(
                SpeedTestProgress(
                    phase = TestPhase.COMPLETE,
                    progress = 1f,
                    message = "Test complete!",
                    currentSpeed = uploadSpeed,
                    pingMs = pingMs,
                    jitterMs = jitterMs,
                    packetLoss = packetLoss,
                    status = TestStatus.SUCCESS
                )
            )

            SpeedTestResult(
                downloadSpeedMbps = downloadSpeed,
                uploadSpeedMbps = uploadSpeed,
                pingMs = pingMs,
                jitterMs = jitterMs,
                packetLoss = packetLoss,
                testServer = selectedServer,
                status = TestStatus.SUCCESS
            )

        } catch (e: Exception) {
            onProgress(
                SpeedTestProgress(
                    phase = TestPhase.FAILED,
                    progress = 0f,
                    message = "Test failed: ${e.message}",
                    status = TestStatus.FAILED
                )
            )
            SpeedTestResult(status = TestStatus.FAILED)
        }
    }

    private suspend fun measurePing(serverUrl: String): Long {
        return withContext(Dispatchers.IO) {
            try {
                val startTime = System.nanoTime()
                val request = Request.Builder()
                    .url(serverUrl)
                    .head()
                    .build()
                val response = client.newCall(request).execute()
                response.close()
                val endTime = System.nanoTime()
                val pingNs = endTime - startTime
                (pingNs / 1_000_000)
            } catch (e: Exception) {
                -1
            }
        }
    }

    private fun calculateJitter(pingResults: List<Long>): Double {
        if (pingResults.size < 2) return 0.0
        var totalDiff = 0.0
        for (i in 1 until pingResults.size) {
            totalDiff += abs(pingResults[i] - pingResults[i - 1])
        }
        return totalDiff / (pingResults.size - 1)
    }

    private suspend fun runDownloadTest(onProgress: (Float, Double) -> Unit): Double {
        return withContext(Dispatchers.IO) {
            try {
                val url = downloadUrls.random()
                val request = Request.Builder().url(url).build()
                val startTime = System.nanoTime()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) return@withContext 0.0

                val body = response.body
                if (body == null) return@withContext 0.0

                val bytes = body.bytes()
                val fileSize = bytes.size.toLong()
                val endTime = System.nanoTime()

                val durationSec = (endTime - startTime) / 1_000_000_000.0
                val speedMbps = (fileSize * 8.0) / durationSec / 1_000_000

                onProgress(1f, speedMbps)
                speedMbps
            } catch (e: Exception) {
                0.0
            }
        }
    }

    private suspend fun runUploadTest(onProgress: (Float, Double) -> Unit): Double {
        return withContext(Dispatchers.IO) {
            try {
                val uploadSize = 2 * 1024 * 1024
                val data = ByteArray(uploadSize) { Random.nextInt(0, 255).toByte() }
                val requestBody = data.toRequestBody("application/octet-stream".toMediaType())

                val startTime = System.nanoTime()
                val request = Request.Builder()
                    .url("https://httpbin.org/post")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                response.close()

                val endTime = System.nanoTime()
                val durationSec = (endTime - startTime) / 1_000_000_000.0
                val speedMbps = (uploadSize * 8.0) / durationSec / 1_000_000

                onProgress(1f, speedMbps)
                speedMbps
            } catch (e: Exception) {
                0.0
            }
        }
    }
}
