package com.rudra.internetspeedtest.feature.neutrality

data class ISPComparison(
    val yourScore: Int,
    val ispAverageScore: Int,
    val globalAverageScore: Int,
    val percentile: Int
)

class ISPComparisonEngine {
    fun compare(currentScore: NeutralityScore, ispName: String): ISPComparison {
        val ispAverage = getAnonymizedISPAverage(ispName)
        val globalAverage = getAnonymizedGlobalAverage()
        val percentile = calculatePercentile(currentScore.score, ispAverage)

        return ISPComparison(
            yourScore = currentScore.score,
            ispAverageScore = ispAverage,
            globalAverageScore = globalAverage,
            percentile = percentile
        )
    }

    private fun getAnonymizedISPAverage(ispName: String): Int = when {
        ispName.contains("Tele2", ignoreCase = true) -> 78
        ispName.contains("Telia", ignoreCase = true) -> 82
        ispName.contains("Telenor", ignoreCase = true) -> 80
        ispName.contains("Comcast", ignoreCase = true) -> 72
        ispName.contains("Verizon", ignoreCase = true) -> 76
        ispName.contains("AT&T", ignoreCase = true) -> 74
        ispName.contains("Vodafone", ignoreCase = true) -> 75
        else -> 77
    }

    private fun getAnonymizedGlobalAverage(): Int = 78

    private fun calculatePercentile(score: Int, ispAvg: Int): Int {
        val diff = score - ispAvg
        return (50 + diff).coerceIn(0, 100)
    }
}
