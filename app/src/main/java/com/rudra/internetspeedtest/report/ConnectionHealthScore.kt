package com.rudra.internetspeedtest.report

import com.rudra.internetspeedtest.core.testing.BufferbloatResult
import com.rudra.internetspeedtest.feature.neutrality.NeutralityScore
import com.rudra.internetspeedtest.feature.realuse.RealUseReport
import com.rudra.internetspeedtest.domain.model.SpeedTestResult

data class HealthScore(
    val score: Int,
    val speedAdequacy: Int,
    val stabilityScore: Int,
    val bufferbloatScore: Int,
    val neutralityScore: Int,
    val realUseScore: Int,
    val grade: String
)

class ConnectionHealthScoreCalculator {
    fun calculate(
        speed: SpeedTestResult,
        bufferbloat: BufferbloatResult,
        neutrality: NeutralityScore,
        realUse: RealUseReport?
    ): HealthScore {
        val speedAdequacy = (speed.downloadSpeedMbps / 100 * 25).toInt().coerceIn(0, 25)

        val stabilityScore = (speed.confidenceScore * 0.25).toInt().coerceIn(0, 25)

        val bufferbloatScore = when (bufferbloat.grade) {
            com.rudra.internetspeedtest.core.testing.BufferbloatGrade.A_PLUS -> 20
            com.rudra.internetspeedtest.core.testing.BufferbloatGrade.A -> 18
            com.rudra.internetspeedtest.core.testing.BufferbloatGrade.B -> 14
            com.rudra.internetspeedtest.core.testing.BufferbloatGrade.C -> 10
            com.rudra.internetspeedtest.core.testing.BufferbloatGrade.D -> 5
            com.rudra.internetspeedtest.core.testing.BufferbloatGrade.F -> 0
        }

        val neutralityScore = (neutrality.score * 0.15).toInt().coerceIn(0, 15)

        val realUseScore = if (realUse != null) {
            var score = 15
            realUse.streamingResult?.let { if (it.stallEvents > 5) score -= 5 }
            realUse.callQuality?.let { if (it.mosScore < 3.0) score -= 5 }
            realUse.gamingResult?.let { if (it.playabilityVerdict.ordinal > 2) score -= 5 }
            score.coerceIn(0, 15)
        } else 0

        val total = (speedAdequacy + stabilityScore + bufferbloatScore + neutralityScore + realUseScore).coerceIn(0, 100)

        val grade = when {
            total >= 90 -> "Excellent"
            total >= 75 -> "Good"
            total >= 55 -> "Fair"
            total >= 35 -> "Poor"
            else -> "Critical"
        }

        return HealthScore(
            score = total,
            speedAdequacy = speedAdequacy,
            stabilityScore = stabilityScore,
            bufferbloatScore = bufferbloatScore,
            neutralityScore = neutralityScore,
            realUseScore = realUseScore,
            grade = grade
        )
    }
}
