package com.rudra.internetspeedtest.report

import com.rudra.internetspeedtest.core.testing.BufferbloatGrade
import com.rudra.internetspeedtest.feature.neutrality.NeutralityScore

data class ISPReportCard(
    val ispName: String,
    val averageSpeed: Double,
    val speedConsistency: Double,
    val bufferbloatGrade: BufferbloatGrade,
    val neutralityScore: Int,
    val userSatisfaction: Double,
    val grade: String
) {
    companion object {
        fun generate(
            ispName: String,
            avgSpeed: Double,
            consistency: Double,
            bufferbloat: BufferbloatGrade,
            neutrality: NeutralityScore
        ): ISPReportCard {
            val satisfaction = (consistency * 0.3 + (neutrality.score / 100.0) * 0.3 +
                    (avgSpeed / 100.0) * 0.2 + (1.0 - bufferbloat.ordinal / 6.0) * 0.2) * 100

            val grade = when {
                satisfaction >= 90 -> "A+"
                satisfaction >= 80 -> "A"
                satisfaction >= 70 -> "B+"
                satisfaction >= 60 -> "B"
                satisfaction >= 50 -> "C+"
                satisfaction >= 40 -> "C"
                satisfaction >= 30 -> "D"
                else -> "F"
            }

            return ISPReportCard(
                ispName = ispName,
                averageSpeed = avgSpeed,
                speedConsistency = consistency,
                bufferbloatGrade = bufferbloat,
                neutralityScore = neutrality.score,
                userSatisfaction = satisfaction,
                grade = grade
            )
        }
    }
}
