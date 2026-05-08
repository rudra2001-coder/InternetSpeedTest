package com.rudra.internetspeedtest.domain.repository

import com.rudra.internetspeedtest.domain.model.SpeedTestProgress
import com.rudra.internetspeedtest.domain.model.SpeedTestResult

interface InternetSpeedTestRepository {
    suspend fun runSpeedTest(
        onProgress: (SpeedTestProgress) -> Unit
    ): SpeedTestResult
}
