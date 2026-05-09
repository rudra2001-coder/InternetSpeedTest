package com.rudra.internetspeedtest.feature.neutrality

import kotlin.math.abs
import kotlin.math.roundToInt

data class ServiceVariance(
    val service: String,
    val baselineSpeed: Double,
    val serviceSpeed: Double,
    val deviationPercent: Double,
    val severity: Severity
) {
    enum class Severity { MINIMAL, MODERATE, SIGNIFICANT, SEVERE }

    companion object {
        fun calculate(baselineSpeed: Double, serviceSpeed: Double): ServiceVariance {
            val deviation = if (baselineSpeed > 0) ((serviceSpeed - baselineSpeed) / baselineSpeed) * 100 else 0.0
            val severity = when {
                abs(deviation) < 10 -> Severity.MINIMAL
                abs(deviation) < 25 -> Severity.MODERATE
                abs(deviation) < 50 -> Severity.SIGNIFICANT
                else -> Severity.SEVERE
            }
            return ServiceVariance(
                service = "",
                baselineSpeed = baselineSpeed,
                serviceSpeed = serviceSpeed,
                deviationPercent = deviation,
                severity = severity
            )
        }
    }
}
