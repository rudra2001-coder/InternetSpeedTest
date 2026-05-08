package com.rudra.internetspeedtest.domain.model

object BufferbloatGrade {
    fun calculate(idlePing: Double, loadedPing: Double): String {
        if (idlePing <= 0 || loadedPing <= 0) return "N/A"
        val increase = loadedPing - idlePing
        return when {
            increase < 30 -> "A+"
            increase < 100 -> "A"
            increase < 200 -> "B"
            increase < 300 -> "C"
            increase < 500 -> "D"
            else -> "F"
        }
    }
}
