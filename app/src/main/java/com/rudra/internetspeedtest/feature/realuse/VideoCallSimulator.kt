package com.rudra.internetspeedtest.feature.realuse

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

enum class CallResolution(val label: String) {
    AUDIO_ONLY("Audio Only"),
    SD_360p("360p"),
    HD_720p("720p"),
    FULL_HD_1080p("1080p")
}

enum class CallVerdict { EXCELLENT, GOOD, ACCEPTABLE, POOR, UNUSABLE }

data class CallQualityResult(
    val resolution: CallResolution,
    val jitterMs: Double,
    val packetLossPercent: Double,
    val mosScore: Double,
    val verdict: CallVerdict
)

@Singleton
class VideoCallSimulator @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    suspend fun simulate(durationSeconds: Int = 20): CallQualityResult = withContext(Dispatchers.IO) {
        val pings = mutableListOf<Double>()
        var totalPackets = 0
        var lostPackets = 0
        val intervalMs = 50L

        repeat(durationSeconds * (1000 / intervalMs.toInt())) {
            try {
                val start = System.nanoTime()
                val request = Request.Builder().url("https://www.google.com").head().build()
                val response = client.newCall(request).execute()
                response.close()
                val rtt = (System.nanoTime() - start) / 1_000_000.0
                pings.add(rtt)
                totalPackets++
            } catch (_: Exception) {
                lostPackets++
                totalPackets++
            }
            delay(intervalMs)
        }

        val avgPing = if (pings.isNotEmpty()) pings.average() else 0.0
        val jitter = if (pings.size > 1) {
            pings.zipWithNext().map { abs(it.first - it.second) }.average()
        } else 0.0
        val packetLoss = if (totalPackets > 0) lostPackets.toDouble() / totalPackets else 0.0

        val mosScore = calculateMOS(avgPing, jitter, packetLoss)

        val resolution = when {
            packetLoss < 0.005 && jitter < 10 -> CallResolution.FULL_HD_1080p
            packetLoss < 0.02 && jitter < 20 -> CallResolution.HD_720p
            packetLoss < 0.05 && jitter < 40 -> CallResolution.SD_360p
            else -> CallResolution.AUDIO_ONLY
        }

        val verdict = when {
            mosScore >= 4.0 -> CallVerdict.EXCELLENT
            mosScore >= 3.5 -> CallVerdict.GOOD
            mosScore >= 3.0 -> CallVerdict.ACCEPTABLE
            mosScore >= 2.0 -> CallVerdict.POOR
            else -> CallVerdict.UNUSABLE
        }

        CallQualityResult(
            resolution = resolution,
            jitterMs = jitter,
            packetLossPercent = (packetLoss * 100),
            mosScore = mosScore,
            verdict = verdict
        )
    }

    private fun calculateMOS(pingMs: Double, jitterMs: Double, packetLoss: Double): Double {
        val r = 93.2 - (pingMs / 100.0) * 2 - (jitterMs / 10.0) * 1 - (packetLoss * 100) * 2.5
        return (1 + 0.035 * r + r * (r - 60) * (100 - r) * 7.0e-6).coerceIn(1.0, 4.5)
    }
}
