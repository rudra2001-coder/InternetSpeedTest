package com.rudra.internetspeedtest.report

data class VerificationData(
    val testId: String,
    val timestamp: Long,
    val downloadSpeed: Double,
    val uploadSpeed: Double,
    val pingMs: Double
) {
    fun toVerificationUrl(): String {
        return "https://speedtest.app/verify/$testId?ts=$timestamp"
    }
}
