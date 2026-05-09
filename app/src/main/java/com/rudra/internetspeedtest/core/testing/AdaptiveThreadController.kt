package com.rudra.internetspeedtest.core.testing

import com.rudra.internetspeedtest.core.network.NetworkType

data class ThreadConfig(
    val count: Int,
    val reason: String
)

class AdaptiveThreadController {

    private var lastNetworkType: NetworkType? = null

    fun determineThreadCount(
        initialLatencyMs: Double,
        networkType: NetworkType,
        signalStrength: Int?,
        estimatedThroughputMbps: Double? = null
    ): ThreadConfig {
        val networkChanged = lastNetworkType != null && lastNetworkType != networkType
        lastNetworkType = networkType

        if (networkChanged) {
            return ThreadConfig(count = 2, reason = "Network type changed, reduced for stability")
        }

        if (estimatedThroughputMbps != null && estimatedThroughputMbps < 1.0) {
            return ThreadConfig(count = 1, reason = "Very slow connection (<1 Mbps), using single thread")
        }

        if (estimatedThroughputMbps != null && estimatedThroughputMbps < 5.0) {
            return ThreadConfig(count = 2, reason = "Slow connection (<5 Mbps), limited threads")
        }

        if (initialLatencyMs <= 0 || initialLatencyMs > 1000) {
            return ThreadConfig(count = 2, reason = "High or unknown latency, conservative approach")
        }

        return when {
            initialLatencyMs > 200 -> ThreadConfig(count = 6, reason = "High latency detected")
            networkType == NetworkType.CELLULAR && (signalStrength ?: 0) < 50 ->
                ThreadConfig(count = 2, reason = "Weak cellular signal")
            networkType == NetworkType.CELLULAR -> ThreadConfig(count = 3, reason = "Cellular network")
            initialLatencyMs < 10 -> ThreadConfig(count = 8, reason = "Low-latency fiber detected")
            else -> ThreadConfig(count = 4, reason = "Standard broadband")
        }
    }

    fun reset() {
        lastNetworkType = null
    }
}
