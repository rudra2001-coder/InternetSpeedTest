package com.rudra.internetspeedtest.feature.neutrality

enum class TemporalConsistency {
    HIGHLY_CONSISTENT,
    MODERATELY_CONSISTENT,
    INCONSISTENT,
    INSUFFICIENT_DATA
}

class TemporalConsistencyChecker {
    suspend fun checkConsistency(currentTest: NeutralityScore, historyDao: com.rudra.internetspeedtest.data.local.dao.TestResultDao): TemporalConsistency {
        val recentTests = historyDao.getLatestResults(limit = 5)
        if (recentTests.isEmpty()) return TemporalConsistency.INSUFFICIENT_DATA

        val scoreVariation = recentTests.map { it.speedMbps }.let { list ->
            if (list.size < 2) return@let 0.0
            val avg = list.average()
            list.map { (it - avg) * (it - avg) }.average()
        }

        return when {
            scoreVariation < 5 -> TemporalConsistency.HIGHLY_CONSISTENT
            scoreVariation < 15 -> TemporalConsistency.MODERATELY_CONSISTENT
            else -> TemporalConsistency.INCONSISTENT
        }
    }
}
