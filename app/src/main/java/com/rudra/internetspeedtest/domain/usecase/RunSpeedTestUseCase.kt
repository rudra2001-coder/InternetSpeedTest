package com.rudra.internetspeedtest.domain.usecase

import com.rudra.internetspeedtest.domain.model.CdnTestProgress
import com.rudra.internetspeedtest.domain.model.TestResult
import com.rudra.internetspeedtest.domain.repository.SpeedTestRepository
import javax.inject.Inject

class RunSpeedTestUseCase @Inject constructor(
    private val speedTestRepository: SpeedTestRepository
) {
    suspend operator fun invoke(
        cdnName: String,
        url: String,
        onProgress: (CdnTestProgress) -> Unit
    ): TestResult {
        return speedTestRepository.runSpeedTest(cdnName, url, onProgress)
    }
}