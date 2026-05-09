package com.rudra.internetspeedtest.data.repository

import com.rudra.internetspeedtest.domain.model.CdnTestProgress
import com.rudra.internetspeedtest.domain.model.TestResult
import com.rudra.internetspeedtest.domain.model.TestStatus
import com.rudra.internetspeedtest.domain.repository.SpeedTestRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeedTestRepositoryImpl @Inject constructor() : SpeedTestRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun runSpeedTest(
        cdnName: String,
        url: String,
        onProgress: (CdnTestProgress) -> Unit
    ): TestResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            onProgress(
                CdnTestProgress(
                    cdnName = cdnName,
                    provider = getProviderName(cdnName),
                    progress = 0f,
                    currentSpeed = 0.0,
                    ttfb = 0,
                    latencyMs = 0,
                    status = TestStatus.RUNNING
                )
            )

            val request = Request.Builder()
                .url(url)
                .build()

            val connectionStart = System.currentTimeMillis()
            val response = client.newCall(request).execute()
            val connectionTime = System.currentTimeMillis() - connectionStart

            if (!response.isSuccessful) {
                return@withContext TestResult(
                    cdnName = cdnName,
                    provider = getProviderName(cdnName),
                    speedMbps = 0.0,
                    ttfbMs = connectionTime,
                    latencyMs = System.currentTimeMillis() - startTime,
                    downloadTimeMs = connectionTime,
                    timestamp = System.currentTimeMillis(),
                    fileSizeBytes = 0,
                    status = TestStatus.FAILED
                )
            }

            val firstByteTime = System.currentTimeMillis() - startTime

            onProgress(
                CdnTestProgress(
                    cdnName = cdnName,
                    provider = getProviderName(cdnName),
                    progress = 0.3f,
                    currentSpeed = 0.0,
                    ttfb = firstByteTime,
                    latencyMs = connectionTime,
                    status = TestStatus.RUNNING
                )
            )

            val body = response.body
            if (body == null) {
                return@withContext TestResult(
                    cdnName = cdnName,
                    provider = getProviderName(cdnName),
                    speedMbps = 0.0,
                    ttfbMs = firstByteTime,
                    latencyMs = System.currentTimeMillis() - startTime,
                    downloadTimeMs = firstByteTime,
                    timestamp = System.currentTimeMillis(),
                    fileSizeBytes = 0,
                    status = TestStatus.FAILED
                )
            }

            val bytes = body.bytes()
            val fileSize = bytes.size.toLong()

            val endTime = System.currentTimeMillis()
            val totalDurationMs = endTime - startTime
            val totalDurationSec = totalDurationMs / 1000.0

            val speedMbps = if (totalDurationSec > 0) (fileSize * 8.0) / totalDurationSec / 1_000_000 else 0.0

            onProgress(
                CdnTestProgress(
                    cdnName = cdnName,
                    provider = getProviderName(cdnName),
                    progress = 1f,
                    currentSpeed = speedMbps,
                    ttfb = firstByteTime,
                    latencyMs = connectionTime,
                    status = TestStatus.SUCCESS
                )
            )

            TestResult(
                cdnName = cdnName,
                provider = getProviderName(cdnName),
                speedMbps = speedMbps,
                ttfbMs = firstByteTime,
                latencyMs = System.currentTimeMillis() - startTime,
                downloadTimeMs = totalDurationMs,
                timestamp = System.currentTimeMillis(),
                fileSizeBytes = fileSize,
                status = TestStatus.SUCCESS
            )
        } catch (e: Exception) {
            onProgress(
                CdnTestProgress(
                    cdnName = cdnName,
                    provider = getProviderName(cdnName),
                    progress = 0f,
                    currentSpeed = 0.0,
                    ttfb = 0,
                    latencyMs = System.currentTimeMillis() - startTime,
                    status = TestStatus.FAILED
                )
            )

            TestResult(
                cdnName = cdnName,
                provider = getProviderName(cdnName),
                speedMbps = 0.0,
                ttfbMs = 0,
                latencyMs = System.currentTimeMillis() - startTime,
                downloadTimeMs = 0,
                timestamp = System.currentTimeMillis(),
                fileSizeBytes = 0,
                status = TestStatus.FAILED
            )
        }
    }

    private fun getProviderName(cdnName: String): String {
        return when {
            cdnName.contains("Cloudflare", ignoreCase = true) && cdnName.contains("2") -> "Global Edge #2"
            cdnName.contains("Cloudflare", ignoreCase = true) -> "Global Edge"
            cdnName.contains("GitHub", ignoreCase = true) -> "Fastly CDN"
            cdnName.contains("jsDelivr", ignoreCase = true) && cdnName.contains("2") -> "Multi-region #2"
            cdnName.contains("jsDelivr", ignoreCase = true) -> "Multi-region"
            cdnName.contains("unpkg", ignoreCase = true) -> "Cloudflare"
            cdnName.contains("CDNJS", ignoreCase = true) -> "Cloudflare"
            cdnName.contains("npm", ignoreCase = true) -> "Cloudflare"
            else -> "CDN Provider"
        }
    }
}