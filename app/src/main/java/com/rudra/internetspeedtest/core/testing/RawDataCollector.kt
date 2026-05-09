package com.rudra.internetspeedtest.core.testing

data class SpeedSample(
    val timestampMs: Long,
    val speedMbps: Double,
    val threadId: Int
)

data class DiscardedSample(
    val valueMbps: Double,
    val reason: String
)

data class TestRawData(
    val serverName: String,
    val threadId: Int,
    val samples: List<SpeedSample>,
    val discardedSamples: List<DiscardedSample>,
    val startTime: Long,
    val endTime: Long
) {
    val totalCollected: Int get() = samples.size
    val totalDiscarded: Int get() = discardedSamples.size
}

data class AggregateTestData(
    val serverName: String,
    val threadCount: Int,
    val allSamples: List<SpeedSample>,
    val discarded: List<DiscardedSample>,
    val testDurationMs: Long,
    val totalBytesTransferred: Long
) {
    val validSamples: List<Double> get() = allSamples.map { it.speedMbps }
    val displaySummary: String
        get() = "${allSamples.size} samples across $threadCount threads (${
            discarded.size
        } discarded: ${discarded.joinToString { d -> d.reason }})"
}

object StatisticalCleaner {
    fun clean(
        rawSamples: List<SpeedSample>,
        discardSlowestPercent: Double = 0.30,
        discardFastestPercent: Double = 0.10
    ): AggregateTestData {
        if (rawSamples.isEmpty()) return AggregateTestData(
            serverName = "", threadCount = 0,
            allSamples = emptyList(), discarded = emptyList(),
            testDurationMs = 0, totalBytesTransferred = 0
        )

        val sorted = rawSamples.sortedBy { it.speedMbps }
        val slowestCount = (sorted.size * discardSlowestPercent).toInt()
        val fastestCount = (sorted.size * discardFastestPercent).toInt()

        val discarded = mutableListOf<DiscardedSample>()
        sorted.take(slowestCount).forEach {
            discarded.add(DiscardedSample(it.speedMbps, "Slowest ${(discardSlowestPercent * 100).toInt()}% (TCP slow start)"))
        }
        sorted.takeLast(fastestCount).forEach {
            discarded.add(DiscardedSample(it.speedMbps, "Fastest ${(discardFastestPercent * 100).toInt()}% (burst)"))
        }

        val valid = sorted.drop(slowestCount).dropLast(fastestCount)
        val threadIds = rawSamples.map { it.threadId }.distinct()
        val duration = (rawSamples.maxOfOrNull { it.timestampMs } ?: 0) - (rawSamples.minOfOrNull { it.timestampMs } ?: 0)

        return AggregateTestData(
            serverName = "",
            threadCount = threadIds.size,
            allSamples = valid,
            discarded = discarded,
            testDurationMs = duration,
            totalBytesTransferred = 0
        )
    }
}
