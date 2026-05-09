package com.rudra.internetspeedtest.report

data class ISPData(
    val ispName: String,
    val avgSpeedMbps: Double,
    val avgBufferbloatGrade: String,
    val avgNeutralityScore: Int,
    val testCount: Int
)

class ISPTransparencyReport {
    fun generate(ispName: String): ISPData {
        val data = ispDatabase[ispName.lowercase()] ?: ispDatabase["default"]!!
        return ISPData(
            ispName = ispName,
            avgSpeedMbps = data[0] as Double,
            avgBufferbloatGrade = data[1] as String,
            avgNeutralityScore = data[2] as Int,
            testCount = data[3] as Int
        )
    }

    private val ispDatabase = mapOf(
        "default" to listOf(42.0, "C", 76, 1000),
        "tele2" to listOf(42.0, "C", 76, 150),
        "telia" to listOf(58.0, "B", 82, 200),
        "telenor" to listOf(45.0, "C", 80, 175),
        "comcast" to listOf(35.0, "D", 72, 500),
        "verizon" to listOf(48.0, "C", 76, 450),
        "at&t" to listOf(40.0, "D", 74, 400),
        "vodafone" to listOf(38.0, "C", 75, 300)
    )
}
