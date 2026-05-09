package com.rudra.internetspeedtest.report

import com.rudra.internetspeedtest.domain.model.SpeedTestResult

data class PeerGroup(
    val isp: String,
    val planSpeed: String,
    val region: String
)

data class PeerComparisonResult(
    val userSpeed: Double,
    val peerAverageSpeed: Double,
    val percentile: Int,
    val message: String
)

class PeerComparison {
    fun compare(userResult: SpeedTestResult, peerGroup: PeerGroup): PeerComparisonResult {
        val peerAvg = estimatePeerAverage(peerGroup, userResult.downloadSpeedMbps)
        val percentile = calculatePercentile(userResult.downloadSpeedMbps, peerAvg)

        val message = when {
            percentile >= 80 -> "You're in the top ${percentile}% of ${peerGroup.isp} users"
            percentile >= 50 -> "You're getting average speeds for ${peerGroup.isp}"
            percentile >= 20 -> "You're below average. ${peerGroup.isp} users typically get higher speeds."
            else -> "Your speed is in the bottom ${100 - percentile}%. You may want to contact ${peerGroup.isp} support."
        }

        return PeerComparisonResult(
            userSpeed = userResult.downloadSpeedMbps,
            peerAverageSpeed = peerAvg,
            percentile = percentile,
            message = message
        )
    }

    private fun estimatePeerAverage(peerGroup: PeerGroup, userSpeed: Double): Double {
        val base = when {
            peerGroup.isp.contains("Tele2", ignoreCase = true) -> 42.0
            peerGroup.isp.contains("Telia", ignoreCase = true) -> 58.0
            peerGroup.isp.contains("Telenor", ignoreCase = true) -> 45.0
            peerGroup.isp.contains("Comcast", ignoreCase = true) -> 35.0
            peerGroup.isp.contains("Verizon", ignoreCase = true) -> 48.0
            peerGroup.isp.contains("AT&T", ignoreCase = true) -> 40.0
            peerGroup.isp.contains("Vodafone", ignoreCase = true) -> 38.0
            else -> 45.0
        }
        val planMultiplier = when {
            peerGroup.planSpeed.contains("100", ignoreCase = true) -> 1.0
            peerGroup.planSpeed.contains("200", ignoreCase = true) -> 1.3
            peerGroup.planSpeed.contains("500", ignoreCase = true) -> 1.6
            peerGroup.planSpeed.contains("1000", ignoreCase = true) -> 2.0
            else -> 1.0
        }
        return base * planMultiplier
    }

    private fun calculatePercentile(userSpeed: Double, peerAvg: Double): Int {
        val diff = userSpeed - peerAvg
        val percentile = 50 + (diff / peerAvg * 50).toInt()
        return percentile.coerceIn(1, 99)
    }
}
