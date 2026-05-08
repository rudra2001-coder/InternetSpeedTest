package com.rudra.internetspeedtest.domain.usecase

import com.rudra.internetspeedtest.domain.model.SpeedTestProgress
import com.rudra.internetspeedtest.domain.model.SpeedTestResult
import com.rudra.internetspeedtest.domain.repository.InternetSpeedTestRepository
import javax.inject.Inject

class RunInternetSpeedTestUseCase @Inject constructor(
    private val repository: InternetSpeedTestRepository
) {
    suspend operator fun invoke(
        onProgress: (SpeedTestProgress) -> Unit
    ): SpeedTestResult {
        return repository.runSpeedTest(onProgress)
    }
}
