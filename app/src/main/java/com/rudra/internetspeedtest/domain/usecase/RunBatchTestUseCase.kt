package com.rudra.internetspeedtest.domain.usecase

import com.rudra.internetspeedtest.domain.model.CdnTestProgress
import com.rudra.internetspeedtest.domain.model.TestResult
import com.rudra.internetspeedtest.domain.model.TestStatus
import com.rudra.internetspeedtest.domain.repository.SpeedTestRepository
import com.rudra.internetspeedtest.domain.repository.TestHistoryRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
        coroutineScope {
            val deferredResults = cdns.map { (cdnName, url) ->
                async {
                    val progress = CdnTestProgress(
                        cdnName = cdnName,
                        provider = getProviderName(cdnName),
                        progress = 0f,
                        currentSpeed = 0.0,
                        ttfb = 0,
                        latencyMs = 0,
                        status = TestStatus.RUNNING
                    )
                    onProgress(progress)

                    val startTime = System.currentTimeMillis()
                    val result = speedTestRepository.runSpeedTest(cdnName, url) { update ->
                        val updatedProgress = update.copy(
                            provider = getProviderName(cdnName),
                            latencyMs = System.currentTimeMillis() - startTime
                        )
                        onProgress(updatedProgress)
                    }
                    val latencyMs = System.currentTimeMillis() - startTime

                    val resultWithLatency = result.copy(latencyMs = latencyMs)
                    historyRepository.insertResult(resultWithLatency)
                    resultWithLatency
                }
            }

            val results = deferredResults.awaitAll()
            val finalResults = results.map { result ->
                TestResult(
                    cdnName = result.cdnName,
                    speedMbps = result.speedMbps,
                    ttfbMs = result.ttfbMs,
                    downloadTimeMs = result.downloadTimeMs,
                    timestamp = result.timestamp,
                    fileSizeBytes = result.fileSizeBytes,
                    status = result.status
                )
            }
            onComplete(finalResults)
        }
    }

    private fun getProviderName(cdnName: String): String {
        return when {
            cdnName.contains("Cloudflare", ignoreCase = true) -> "Global Edge"
            cdnName.contains("GitHub", ignoreCase = true) -> "Fastly CDN"
            cdnName.contains("jsDelivr", ignoreCase = true) -> "Multi-region"
            cdnName.contains("unpkg", ignoreCase = true) -> "Cloudflare"
            cdnName.contains("CDNJS", ignoreCase = true) -> "Cloudflare"
            cdnName.contains("npm", ignoreCase = true) -> "Cloudflare"
            cdnName.contains("2", ignoreCase = true) && cdnName.contains("Cloudflare", ignoreCase = true) -> "Global Edge #2"
            else -> "CDN Provider"
        }
    }
}