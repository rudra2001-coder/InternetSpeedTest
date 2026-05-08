package com.rudra.internetspeedtest.domain.model

data class TestResult(
    val id: Long = 0,
    val cdnName: String,
    val speedMbps: Double,
    val ttfbMs: Long,
    val downloadTimeMs: Long,
    val timestamp: Long,
    val fileSizeBytes: Long,
    val status: TestStatus
)