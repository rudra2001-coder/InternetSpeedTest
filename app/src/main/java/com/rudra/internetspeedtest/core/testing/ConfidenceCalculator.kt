package com.rudra.internetspeedtest.core.testing

import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class ConfidenceBreakdown(
    val variancePenalty: Int,
    val threadPenalty: Int,
    val packetLossPenalty: Int,
    val bufferbloatPenalty: Int
)

data class ConfidenceResult(
    val score: Int,
    val breakdown: ConfidenceBreakdown
)

class ConfidenceCalculator {
    fun calculate(
        samples: List<Double>,
        variance: Double,
        threadConsistency: Double,
        packetLoss: Double,
        bufferbloatGrade: BufferbloatGrade
    ): ConfidenceResult {
        var score = 100.0

        val variancePenalty = (variance / 10).coerceAtMost(30.0).roundToInt()
        score -= variancePenalty

        val threadPenalty = if (threadConsistency < 0.7) 20 else 0
        score -= threadPenalty

        val packetLossPenalty = (packetLoss * 100).coerceAtMost(15.0).roundToInt()
        score -= packetLossPenalty

        val bufferbloatPenalty = if (bufferbloatGrade.ordinal >= BufferbloatGrade.D.ordinal) 15 else 0
        score -= bufferbloatPenalty

        return ConfidenceResult(
            score = score.coerceIn(0.0, 100.0).roundToInt(),
            breakdown = ConfidenceBreakdown(
                variancePenalty = variancePenalty,
                threadPenalty = threadPenalty,
                packetLossPenalty = packetLossPenalty,
                bufferbloatPenalty = bufferbloatPenalty
            )
        )
    }

    fun calculateFromSamples(samples: List<Double>, packetLoss: Double, bufferbloatGrade: BufferbloatGrade): ConfidenceResult {
        if (samples.isEmpty()) return ConfidenceResult(0, ConfidenceBreakdown(0, 0, 0, 0))

        val avg = samples.average()
        val variance = samples.map { (it - avg) * (it - avg) }.average()
        val stdDev = sqrt(variance)
        val coeffVar = if (avg > 0) stdDev / avg else 1.0

        var score = 100.0
        val variancePenalty = (coeffVar * 30).roundToInt().coerceIn(0, 30)
        score -= variancePenalty

        val threadPenalty = if (coeffVar > 0.5) 20 else 0
        score -= threadPenalty

        val packetLossPenalty = (packetLoss * 100).roundToInt().coerceAtMost(15)
        score -= packetLossPenalty

        val bufferbloatPenalty = if (bufferbloatGrade.ordinal >= BufferbloatGrade.D.ordinal) 15 else 0
        score -= bufferbloatPenalty

        return ConfidenceResult(
            score = score.coerceIn(0.0, 100.0).roundToInt(),
            breakdown = ConfidenceBreakdown(
                variancePenalty = variancePenalty,
                threadPenalty = threadPenalty,
                packetLossPenalty = packetLossPenalty,
                bufferbloatPenalty = bufferbloatPenalty
            )
        )
    }
}
