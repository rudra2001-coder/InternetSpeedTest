package com.rudra.internetspeedtest.domain.model

data class TestResult(
    val id: Long = 0,
    val cdnName: String,
    val provider: String = "",
    val speedMbps: Double,
    val ttfbMs: Long,
    val latencyMs: Long = 0,
    val downloadTimeMs: Long,
    val timestamp: Long,
    val fileSizeBytes: Long,
    val status: TestStatus
)