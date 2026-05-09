package com.rudra.internetspeedtest.core.testing

import java.util.UUID

data class TestProvenance(
    val serversUsed: List<String>,
    val threadsPerServer: Int,
    val totalSamplesCollected: Int,
    val samplesDiscarded: Int,
    val testDurationSeconds: Int,
    val dataTransferredMB: Double,
    val testId: String = UUID.randomUUID().toString().take(8).uppercase(),
    val timestamp: Long = System.currentTimeMillis()
) {
    val displayString: String
        get() = "Test #$testId | ${serversUsed.size} servers x $threadsPerServer threads | $totalSamplesCollected samples ($samplesDiscarded cleaned) | ${String.format("%.0f", dataTransferredMB)}MB transferred | ${testDurationSeconds}s duration"
}
