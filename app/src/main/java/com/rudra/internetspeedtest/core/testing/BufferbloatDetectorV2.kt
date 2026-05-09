package com.rudra.internetspeedtest.core.testing

enum class BufferbloatGrade(val label: String, val threshold: Int) {
    A_PLUS("Excellent", 0),
    A("Good", 30),
    B("Moderate", 100),
    C("Concerning", 200),
    D("Poor", 300),
    F("Critical", 500);

    companion object {
        fun fromIncrease(increaseMs: Double): BufferbloatGrade {
            return when {
                increaseMs < 30 -> A_PLUS
                increaseMs < 100 -> A
                increaseMs < 200 -> B
                increaseMs < 300 -> C
                increaseMs < 500 -> D
                else -> F
            }
        }
    }
}

data class BufferbloatResult(
    val idlePingMs: Double,
    val downloadLoadedPingMs: Double,
    val uploadLoadedPingMs: Double,
    val grade: BufferbloatGrade,
    val downloadIncreasePercent: Int,
    val uploadIncreasePercent: Int,
    val verdict: String
) {
    companion object {
        fun calculate(idlePingMs: Double, downloadLoadedPingMs: Double, uploadLoadedPingMs: Double): BufferbloatResult {
            val downloadInc = if (idlePingMs > 0) ((downloadLoadedPingMs - idlePingMs) / idlePingMs * 100).toInt() else 0
            val uploadInc = if (idlePingMs > 0) ((uploadLoadedPingMs - idlePingMs) / idlePingMs * 100).toInt() else 0
            val maxLoaded = maxOf(downloadLoadedPingMs, uploadLoadedPingMs)
            val increase = maxLoaded - idlePingMs
            val grade = BufferbloatGrade.fromIncrease(increase)
            val verdict = "Idle: ${idlePingMs.toInt()}ms -> Under load: ${downloadLoadedPingMs.toInt()}ms download / ${uploadLoadedPingMs.toInt()}ms upload | Grade: ${grade.name} (${
                grade.label
            })"
            return BufferbloatResult(
                idlePingMs = idlePingMs,
                downloadLoadedPingMs = downloadLoadedPingMs,
                uploadLoadedPingMs = uploadLoadedPingMs,
                grade = grade,
                downloadIncreasePercent = downloadInc,
                uploadIncreasePercent = uploadInc,
                verdict = verdict
            )
        }
    }
}
