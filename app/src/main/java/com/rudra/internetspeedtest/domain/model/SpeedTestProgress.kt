package com.rudra.internetspeedtest.domain.model

data class SpeedTestProgress(
    val phase: TestPhase = TestPhase.IDLE,
    val progress: Float = 0f,
    val currentSpeed: Double = 0.0,
    val pingMs: Double = 0.0,
    val loadedPingMs: Double = 0.0,
    val jitterMs: Double = 0.0,
    val packetLoss: Double = 0.0,
    val bufferbloatGrade: String = "",
    val confidenceScore: Int = 0,
    val speedSamples: List<Double> = emptyList(),
    val status: TestStatus = TestStatus.IDLE,
    val message: String = "",
    val connectionType: String = "",
    val ispName: String = ""
)
