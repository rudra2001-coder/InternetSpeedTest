package com.rudra.internetspeedtest.domain.usecase

import com.rudra.internetspeedtest.domain.model.CdnTestProgress
import com.rudra.internetspeedtest.domain.model.TestResult
import com.rudra.internetspeedtest.domain.model.TestStatus
import com.rudra.internetspeedtest.domain.repository.SpeedTestRepository
import com.rudra.internetspeedtest.domain.repository.TestHistoryRepository
import javax.inject.Inject

class RunBatchTestUseCase @Inject constructor(
    private val speedTestRepository: SpeedTestRepository,
    private val historyRepository: TestHistoryRepository
) {
    suspend operator fun invoke(
        cdns: List<Pair<String, String>>,
        onProgress: (CdnTestProgress) -> Unit,
        onComplete: (List<TestResult>) -> Unit
    ) {
        val retryCount = 3
        val results = mutableListOf<TestResult>()

        for ((cdnName, url) in cdns) {
            val cdnResults = mutableListOf<TestResult>()

            for (retry in 1..retryCount) {
                onProgress(
                    CdnTestProgress(
                        cdnName = cdnName,
                        progress = 0f,
                        currentSpeed = 0.0,
                        ttfb = 0,
                        status = TestStatus.RUNNING
                    )
                )

                val result = speedTestRepository.runSpeedTest(cdnName, url) { progress ->
                    onProgress(progress.copy(progress = retry.toFloat() / retryCount))
                }

                cdnResults.add(result)

                if (result.status == TestStatus.SUCCESS) {
                    historyRepository.insertResult(result)
                }
            }

            val avgSpeed = cdnResults.filter { it.status == TestStatus.SUCCESS }
                .map { it.speedMbps }
                .average()

            val avgTtfb = cdnResults.filter { it.status == TestStatus.SUCCESS }
                .map { it.ttfbMs }
                .average()

            val avgResult = TestResult(
                cdnName = cdnName,
                speedMbps = avgSpeed,
                ttfbMs = avgTtfb.toLong(),
                downloadTimeMs = cdnResults.firstOrNull()?.downloadTimeMs ?: 0,
                timestamp = System.currentTimeMillis(),
                fileSizeBytes = cdnResults.firstOrNull()?.fileSizeBytes ?: 0,
                status = if (cdnResults.any { it.status == TestStatus.SUCCESS })
                    TestStatus.SUCCESS
                else
                    TestStatus.FAILED
            )

            results.add(avgResult)
            historyRepository.insertResult(avgResult)
        }

        onComplete(results)
    }
}