package com.rudra.internetspeedtest.feature.realuse

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

enum class GamingVerdict(val label: String) {
    EXCELLENT("Competitive gaming ready"),
    GOOD("Casual gaming ready"),
    ACCEPTABLE("Playable with occasional lag"),
    POOR("Frequent lag spikes"),
    UNSUITABLE("Not suitable for gaming")
}

data class GamingResult(
    val averagePingMs: Double,
    val pingJitterMs: Double,
    val pingSpikes: Int,
    val packetLoss: Double,
    val playabilityVerdict: GamingVerdict
)

@Singleton
class GamingLatencySimulator @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .build()

    suspend fun simulate(durationSeconds: Int = 30): GamingResult = withContext(Dispatchers.IO) {
        val pings = mutableListOf<Double>()
        var lost = 0

        repeat(durationSeconds * 10) {
            try {
                val start = System.nanoTime()
                val request = Request.Builder().url("https://www.google.com").head().build()
                val response = client.newCall(request).execute()
                response.close()
                val rtt = (System.nanoTime() - start) / 1_000_000.0
                pings.add(rtt)
            } catch (_: Exception) {
                lost++
            }
            delay(100)
        }

        val avgPing = if (pings.isNotEmpty()) pings.average() else 0.0
        val validPings = pings.filter { it > 0 }
        val jitter = if (validPings.size > 1) {
            validPings.zipWithNext().map { kotlin.math.abs(it.first - it.second) }.average()
        } else 0.0
        val spikeThreshold = avgPing * 2
        val spikes = validPings.count { it > spikeThreshold }.coerceAtLeast(0)
        val packetLoss = if (pings.isNotEmpty()) lost.toDouble() / (pings.size + lost) else 0.0

        val verdict = when {
            avgPing < 30 && jitter < 5 && spikes <= 2 && packetLoss < 0.01 -> GamingVerdict.EXCELLENT
            avgPing < 60 && jitter < 15 && spikes <= 5 && packetLoss < 0.02 -> GamingVerdict.GOOD
            avgPing < 120 && jitter < 30 && spikes <= 10 && packetLoss < 0.05 -> GamingVerdict.ACCEPTABLE
            avgPing < 200 && spikes <= 20 -> GamingVerdict.POOR
            else -> GamingVerdict.UNSUITABLE
        }

        GamingResult(
            averagePingMs = avgPing,
            pingJitterMs = jitter,
            pingSpikes = spikes,
            packetLoss = packetLoss,
            playabilityVerdict = verdict
        )
    }
}
