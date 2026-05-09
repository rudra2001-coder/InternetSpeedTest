package com.rudra.internetspeedtest.feature.realuse

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class NetflixStreamingResult(
    val speedMbps: Double,
    val hdrReady: Boolean,
    val q4kReady: Boolean,
    val verdict: String
)

@Singleton
class NetflixStreamingSimulator @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun simulate(): NetflixStreamingResult = withContext(Dispatchers.IO) {
        val speeds = mutableListOf<Double>()

        repeat(3) {
            try {
                val request = Request.Builder().url("https://fast.com").build()
                val start = System.nanoTime()
                val response = client.newCall(request).execute()
                val bytes = response.body?.bytes()
                response.close()
                val duration = (System.nanoTime() - start) / 1_000_000_000.0
                if (duration > 0 && bytes != null) {
                    speeds.add((bytes.size * 8.0) / duration / 1_000_000)
                }
            } catch (_: Exception) { }
        }

        val avgSpeed = if (speeds.isNotEmpty()) speeds.average() else 0.0
        val q4kReady = avgSpeed >= 25
        val hdrReady = avgSpeed >= 30

        val verdict = when {
            avgSpeed >= 30 -> "Netflix 4K HDR ready | Consistent ${String.format("%.0f", avgSpeed)} Mbps"
            avgSpeed >= 25 -> "Netflix 4K ready | Consistent ${String.format("%.0f", avgSpeed)} Mbps"
            avgSpeed >= 5 -> "Netflix HD ready"
            avgSpeed >= 3 -> "Netflix SD ready"
            else -> "Netflix may buffer frequently"
        }

        NetflixStreamingResult(
            speedMbps = avgSpeed,
            hdrReady = hdrReady,
            q4kReady = q4kReady,
            verdict = verdict
        )
    }
}
