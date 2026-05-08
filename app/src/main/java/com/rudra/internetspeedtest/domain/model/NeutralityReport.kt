package com.rudra.internetspeedtest.domain.model

data class NeutralityReport(
    val baselineSpeedMbps: Double = 0.0,
    val serviceResults: List<ServiceTestResult> = emptyList(),
    val neutralityScore: Int = 100,
    val variationDetected: Boolean = false,
    val summary: String = "",
    val recommendation: String = ""
)

data class ServiceTestResult(
    val serviceName: String,
    val endpoint: String,
    val speedMbps: Double = 0.0,
    val deviationPercent: Double = 0.0,
    val status: String = "normal"  // normal, elevated, reduced
)
