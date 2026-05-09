package com.rudra.internetspeedtest.domain.model

import com.rudra.internetspeedtest.core.testing.TestPhase
import com.rudra.internetspeedtest.core.testing.TestProvenance

data class SpeedTestProgress(
    val phase: TestPhase = TestPhase.ServerSelection,
    val progress: Float = 0f,
    val currentSpeed: Double = 0.0,
    val pingMs: Double = 0.0,
    val loadedPingMs: Double = 0.0,
    val jitterMs: Double = 0.0,
    val packetLoss: Double = 0.0,
    val bufferbloatGrade: String = "",
    val confidenceScore: Int = 0,
    val speedSamples: List<Double> = emptyList(),
    val threadConfig: String = "",
    val status: TestStatus = TestStatus.IDLE,
    val message: String = "",
    val connectionType: String = "",
    val ispName: String = "",
    val procedure: TestProvenance? = null,
    val selectedServers: List<String> = emptyList()
)
