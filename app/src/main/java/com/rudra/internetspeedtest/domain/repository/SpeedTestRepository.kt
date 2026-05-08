package com.rudra.internetspeedtest.domain.repository

import com.rudra.internetspeedtest.domain.model.CdnTestProgress
import com.rudra.internetspeedtest.domain.model.TestResult

interface SpeedTestRepository {
    suspend fun runSpeedTest(
        cdnName: String,
        url: String,
        onProgress: (CdnTestProgress) -> Unit
    ): TestResult
}
