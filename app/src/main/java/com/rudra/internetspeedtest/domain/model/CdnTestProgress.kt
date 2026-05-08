package com.rudra.internetspeedtest.domain.model

data class CdnTestProgress(
    val cdnName: String,
    val progress: Float,
    val currentSpeed: Double,
    val ttfb: Long,
    val status: TestStatus
)