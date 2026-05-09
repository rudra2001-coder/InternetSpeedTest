package com.rudra.internetspeedtest.feature.realuse

import com.rudra.internetspeedtest.core.testing.BufferbloatGrade

data class Requirement(
    val name: String,
    val required: Double,
    val actual: Double,
    val met: Boolean
)

data class StreamingReadiness(
    val service: String,
    val q4kReady: Boolean,
    val hdrReady: Boolean,
    val requirements: List<Requirement>
)

class StreamingReadinessChecker {
    fun checkService(service: String, downloadSpeed: Double, stability: Double, bufferbloatGrade: BufferbloatGrade): StreamingReadiness {
        val speedReq = Requirement("Speed (25 Mbps required)", 25.0, downloadSpeed, downloadSpeed >= 25)
        val stabilityReq = Requirement("Stability (CoV < 30%)", 0.3, stability, stability < 0.3)
        val bufferbloatReq = Requirement(
            "Bufferbloat (Grade B+)",
            1.0,
            bufferbloatGrade.ordinal.toDouble(),
            bufferbloatGrade.ordinal <= BufferbloatGrade.B.ordinal
        )

        val allMet = listOf(speedReq, stabilityReq, bufferbloatReq).all { it.met }

        return StreamingReadiness(
            service = service,
            q4kReady = allMet,
            hdrReady = speedReq.met && stabilityReq.met,
            requirements = listOf(speedReq, stabilityReq, bufferbloatReq)
        )
    }
}
