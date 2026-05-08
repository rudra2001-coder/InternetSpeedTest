package com.rudra.internetspeedtest.domain.model

data class ServiceSpeedResult(
    val serviceName: String,
    val downloadSpeedMbps: Double,
    val uploadSpeedMbps: Double,
    val pingMs: Double,
    val isManipulated: Boolean = false,
    val manipulationType: String = ""
)

data class NetworkNeutralityReport(
    val overallDownloadMbps: Double,
    val overallUploadMbps: Double,
    val overallPingMs: Double,
    val serviceResults: List<ServiceSpeedResult>,
    val ispManipulationDetected: Boolean,
    val manipulationSummary: String,
    val neutralityScore: Int // 0-100
)
