package com.rudra.internetspeedtest.feature.neutrality

import kotlin.math.abs
import kotlin.math.roundToInt

data class NeutralityScore(
    val score: Int,
    val avgDeviation: Int,
    val pattern: String
)

class NeutralityScoreEngine {
    fun calculate(varianceList: List<ServiceVariance>): NeutralityScore {
        if (varianceList.isEmpty()) return NeutralityScore(100, 0, "No data")

        val avgAbsDeviation = varianceList.map { abs(it.deviationPercent) }.average()
        val allPositive = varianceList.all { it.deviationPercent > 0 }
        val allNegative = varianceList.all { it.deviationPercent < 0 }
        val hasExtremeOutlier = varianceList.any { abs(it.deviationPercent) > 50 }

        var score = 100.0
        score -= avgAbsDeviation * 0.6
        if (allPositive || allNegative) score -= 15
        if (hasExtremeOutlier) score -= 10

        val pattern = when {
            allPositive -> "Consistent acceleration"
            allNegative -> "Consistent depression"
            else -> "Mixed variation"
        }

        return NeutralityScore(
            score = score.coerceIn(0.0, 100.0).roundToInt(),
            avgDeviation = avgAbsDeviation.roundToInt(),
            pattern = pattern
        )
    }
}
