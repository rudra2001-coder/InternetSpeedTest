package com.rudra.internetspeedtest.domain.model

import com.rudra.internetspeedtest.core.testing.BufferbloatGrade as CoreBufferbloatGrade

object BufferbloatGrade {
    fun calculate(idlePing: Double, loadedPing: Double): String {
        if (idlePing <= 0 || loadedPing <= 0) return "N/A"
        val increase = loadedPing - idlePing
        return CoreBufferbloatGrade.fromIncrease(increase).name
    }
}
