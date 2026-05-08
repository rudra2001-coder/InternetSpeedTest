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
        try {
            onProgress(
                CdnTestProgress(
                    cdnName = cdnName,
                    progress = 0f,
                    currentSpeed = 0.0,
                    ttfb = 0,
                    status = TestStatus.RUNNING
                )
            )

            val request = Request.Builder()
                .url(url)
                .build()

            val startTime = System.nanoTime()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext TestResult(
                    cdnName = cdnName,
                    speedMbps = 0.0,
                    ttfbMs = 0,
                    downloadTimeMs = 0,
                    timestamp = System.currentTimeMillis(),
                    fileSizeBytes = 0,
                    status = TestStatus.FAILED
                )
            }

            val firstByteTime = System.nanoTime()
            val ttfbNs = firstByteTime - startTime
            val ttfbMs = ttfbNs / 1_000_000

            onProgress(
                CdnTestProgress(
                    cdnName = cdnName,
                    progress = 0.3f,
                    currentSpeed = 0.0,
                    ttfb = ttfbMs,
                    status = TestStatus.RUNNING
                )
            )

            val body = response.body
            if (body == null) {
                return@withContext TestResult(
                    cdnName = cdnName,
                    speedMbps = 0.0,
                    ttfbMs = ttfbMs,
                    downloadTimeMs = 0,
                    timestamp = System.currentTimeMillis(),
                    fileSizeBytes = 0,
                    status = TestStatus.FAILED
                )
            }

            val bytes = body.bytes()
            val fileSize = bytes.size.toLong()

            val endTime = System.nanoTime()
            val totalDurationNs = endTime - startTime
            val totalDurationMs = totalDurationNs / 1_000_000
            val totalDurationSec = totalDurationNs / 1_000_000_000.0

            val speedMbps = (fileSize * 8.0) / totalDurationSec / 1_000_000

            onProgress(
                CdnTestProgress(
                    cdnName = cdnName,
                    progress = 1f,
                    currentSpeed = speedMbps,
                    ttfb = ttfbMs,
                    status = TestStatus.SUCCESS
                )
            )

            TestResult(
                cdnName = cdnName,
                speedMbps = speedMbps,
                ttfbMs = ttfbMs,
                downloadTimeMs = totalDurationMs,
                timestamp = System.currentTimeMillis(),
                fileSizeBytes = fileSize,
                status = TestStatus.SUCCESS
            )
        } catch (e: Exception) {
            onProgress(
                CdnTestProgress(
                    cdnName = cdnName,
                    progress = 0f,
                    currentSpeed = 0.0,
                    ttfb = 0,
                    status = TestStatus.FAILED
                )
            )

            TestResult(
                cdnName = cdnName,
                speedMbps = 0.0,
                ttfbMs = 0,
                downloadTimeMs = 0,
                timestamp = System.currentTimeMillis(),
                fileSizeBytes = 0,
                status = TestStatus.FAILED
            )
        }
    }

    override suspend fun runBatchTest(
        cdns: List<Pair<String, String>>,
        onProgress: (CdnTestProgress) -> Unit,
        onComplete: (List<TestResult>) -> Unit
    ) {
        val results = mutableListOf<TestResult>()

        for ((index, cdn) in cdns.withIndex()) {
            val result = runSpeedTest(cdn.first, cdn.second) { progress ->
                onProgress(progress.copy(progress = (index.toFloat() + progress.progress) / cdns.size))
            }
            results.add(result)
        }

        onComplete(results)
    }
}