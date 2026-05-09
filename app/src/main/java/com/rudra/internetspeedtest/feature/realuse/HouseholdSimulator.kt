package com.rudra.internetspeedtest.feature.realuse

import com.rudra.internetspeedtest.domain.model.SpeedTestResult
import kotlin.math.floor

data class HouseholdCapacity(
    val simultaneous4KStreams: Int,
    val simultaneousVideoCalls: Int,
    val simultaneousGamingSessions: Int,
    val recommendation: String
)

class HouseholdSimulator {
    fun calculate(testResult: SpeedTestResult): HouseholdCapacity {
        val download = testResult.downloadSpeedMbps
        val confidence = testResult.confidenceScore / 100.0
        val effectiveSpeed = download * (0.7 + confidence * 0.3)

        val overheadFactor = 0.8
        val usableSpeed = effectiveSpeed * overheadFactor

        val kb4kStreams = floor(usableSpeed / 25.0).toInt().coerceAtLeast(0)
        val videoCalls = floor(usableSpeed / 5.0).toInt().coerceAtLeast(0)
        val gamingSessions = floor(usableSpeed / 10.0).toInt().coerceAtLeast(0)

        val recommendation = buildString {
            append("Your connection can support")
            val activities = mutableListOf<String>()
            if (kb4kStreams > 0) activities.add("${kb4kStreams}x 4K streams")
            if (videoCalls > 0) activities.add("${videoCalls}x video calls")
            if (gamingSessions > 0) activities.add("${gamingSessions}x gaming sessions")
            append(" ${activities.joinToString(" + ")} simultaneously")
            if (confidence < 0.5) append(" (estimates may vary due to connection instability)")
        }

        return HouseholdCapacity(
            simultaneous4KStreams = kb4kStreams,
            simultaneousVideoCalls = videoCalls,
            simultaneousGamingSessions = gamingSessions,
            recommendation = recommendation
        )
    }
}
