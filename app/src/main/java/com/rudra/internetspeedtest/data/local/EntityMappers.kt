package com.rudra.internetspeedtest.data.local

import com.rudra.internetspeedtest.data.local.entity.TestResultEntity
import com.rudra.internetspeedtest.domain.model.TestResult
import com.rudra.internetspeedtest.domain.model.TestStatus

fun TestResultEntity.toDomain(): TestResult {
    return TestResult(
        id = id,
        cdnName = cdnName,
        speedMbps = speedMbps,
        ttfbMs = ttfbMs,
        downloadTimeMs = downloadTimeMs,
        timestamp = timestamp,
        fileSizeBytes = fileSizeBytes,
        status = TestStatus.valueOf(status)
    )
}

fun TestResult.toEntity(): TestResultEntity {
    return TestResultEntity(
        id = id,
        cdnName = cdnName,
        speedMbps = speedMbps,
        ttfbMs = ttfbMs,
        downloadTimeMs = downloadTimeMs,
        timestamp = timestamp,
        fileSizeBytes = fileSizeBytes,
        status = status.name
    )
}