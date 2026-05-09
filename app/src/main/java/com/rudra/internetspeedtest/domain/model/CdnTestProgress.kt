package com.rudra.internetspeedtest.domain.model

data class CdnTestProgress(
    val cdnName: String,
    val provider: String = "",
    val progress: Float,
    val currentSpeed: Double,
    val ttfb: Long,
    val latencyMs: Long = 0,
    val status: TestStatus
)