package com.rudra.internetspeedtest.domain.model

data class SpeedTestResult(
    val downloadSpeedMbps: Double = 0.0,
    val uploadSpeedMbps: Double = 0.0,
    val pingMs: Double = 0.0,
    val loadedDownloadPingMs: Double = 0.0,
    val loadedUploadPingMs: Double = 0.0,
    val jitterMs: Double = 0.0,
    val packetLoss: Double = 0.0,
    val bufferbloatGrade: String = "N/A",
    val confidenceScore: Int = 0,
    val connectionType: String = "",
    val ispName: String = "",
    val isCgnat: Boolean = false,
    val testServer: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: TestStatus = TestStatus.IDLE,
    val rawThreadSpeeds: List<Double> = emptyList(),
    val speedSamples: List<Double> = emptyList()
)
