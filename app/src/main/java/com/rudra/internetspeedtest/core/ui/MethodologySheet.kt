package com.rudra.internetspeedtest.core.ui

data class MethodologyStep(val number: String, val title: String, val description: String)

object MethodologyData {
    val steps = listOf(
        MethodologyStep("1", "Server Selection", "We ping 4 different servers and select the 3 fastest."),
        MethodologyStep("2", "Parallel Testing", "Multiple connections run simultaneously to saturate your link."),
        MethodologyStep("3", "Statistical Cleaning", "We remove fastest 10% and slowest 30% to eliminate noise."),
        MethodologyStep("4", "Bufferbloat Check", "We measure latency under load to expose hidden problems."),
        MethodologyStep("5", "Confidence Rating", "We score result reliability based on consistency.")
    )
}
