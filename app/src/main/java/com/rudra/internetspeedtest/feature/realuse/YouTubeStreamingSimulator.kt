package com.rudra.internetspeedtest.feature.realuse

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

enum class VideoResolution(val label: String, val requiredMbps: Double) {
    SD_480p("480p", 3.0),
    HD_720p("720p", 5.0),
    FULL_HD_1080p("1080p", 8.0),
    QHD_1440p("1440p", 16.0),
    UHD_4K("4K", 25.0),
    UHD_8K("8K", 50.0)
}

enum class StreamingVerdict { EXCELLENT, GOOD, ADEQUATE, UNSTABLE, UNSUITABLE }

data class StreamingSimResult(
    val stableResolution: VideoResolution,
    val bufferHealthPercent: Int,
    val stallEvents: Int,
    val initialBufferingSeconds: Double,
    val qualitySwitches: Int,
    val recommendation: StreamingVerdict
)

@Singleton
class YouTubeStreamingSimulator @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun simulate(durationSeconds: Int = 30): StreamingSimResult = withContext(Dispatchers.IO) {
        val speeds = mutableListOf<Double>()
        val startTime = System.currentTimeMillis()

        repeat(durationSeconds) {
            try {
                val chunkSize = 250 * 1024
                val request = Request.Builder()
                    .url("https://redirector.googlevideo.com")
                    .build()
                val start = System.nanoTime()
                val response = client.newCall(request).execute()
                val bytes = response.body?.bytes()?.size ?: 0
                response.close()
                val duration = (System.nanoTime() - start) / 1_000_000_000.0
                if (duration > 0) {
                    speeds.add((bytes * 8.0) / duration / 1_000_000)
                }
            } catch (_: Exception) {
                speeds.add(0.0)
            }
        }

        val avgSpeed = if (speeds.isNotEmpty()) speeds.average() else 0.0
        val minSpeed = if (speeds.isNotEmpty()) speeds.min() else 0.0
        val stallCount = speeds.count { it < 1.0 }

        val stableResolution = VideoResolution.entries.lastOrNull { avgSpeed >= it.requiredMbps * 1.2 }
            ?: VideoResolution.SD_480p

        val bufferHealth = ((avgSpeed / stableResolution.requiredMbps) * 100).toInt().coerceIn(0, 100)
        val qualitySwitches = speeds.zipWithNext().count { (a, b) ->
            val resA = VideoResolution.entries.lastOrNull { a >= it.requiredMbps }?.ordinal ?: 0
            val resB = VideoResolution.entries.lastOrNull { b >= it.requiredMbps }?.ordinal ?: 0
            resA != resB
        }

        val recommendation = when {
            stallCount <= 1 && avgSpeed >= stableResolution.requiredMbps * 1.5 -> StreamingVerdict.EXCELLENT
            stallCount <= 2 && avgSpeed >= stableResolution.requiredMbps -> StreamingVerdict.GOOD
            stallCount <= 5 && minSpeed > 1.0 -> StreamingVerdict.ADEQUATE
            stallCount <= 10 -> StreamingVerdict.UNSTABLE
            else -> StreamingVerdict.UNSUITABLE
        }

        StreamingSimResult(
            stableResolution = stableResolution,
            bufferHealthPercent = bufferHealth,
            stallEvents = stallCount,
            initialBufferingSeconds = if (avgSpeed > 0) 1.0 / avgSpeed.coerceAtLeast(0.5) else 5.0,
            qualitySwitches = qualitySwitches,
            recommendation = recommendation
        )
    }
}
