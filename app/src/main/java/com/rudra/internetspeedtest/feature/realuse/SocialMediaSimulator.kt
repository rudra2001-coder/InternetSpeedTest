package com.rudra.internetspeedtest.feature.realuse

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class SocialMediaResult(
    val imageLoadTimeMs: Double,
    val videoPreloadTimeMs: Double,
    val scrollSmoothness: Int,
    val verdict: String
)

@Singleton
class SocialMediaSimulator @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun simulate(): SocialMediaResult = withContext(Dispatchers.IO) {
        val imageTimes = mutableListOf<Double>()
        val videoTimes = mutableListOf<Double>()

        repeat(5) {
            try {
                val start = System.nanoTime()
                val request = Request.Builder().url("https://i.instagram.com").build()
                val response = client.newCall(request).execute()
                response.body?.bytes()
                response.close()
                imageTimes.add((System.nanoTime() - start) / 1_000_000.0)
            } catch (_: Exception) { }
        }

        repeat(3) {
            try {
                val start = System.nanoTime()
                val request = Request.Builder().url("https://instagram.com").build()
                val response = client.newCall(request).execute()
                response.body?.bytes()
                response.close()
                videoTimes.add((System.nanoTime() - start) / 1_000_000.0)
            } catch (_: Exception) { }
        }

        val avgImageLoad = if (imageTimes.isNotEmpty()) imageTimes.average() else 1000.0
        val avgVideoPreload = if (videoTimes.isNotEmpty()) videoTimes.average() else 2000.0

        val smoothness = when {
            avgImageLoad < 300 -> 95
            avgImageLoad < 600 -> 80
            avgImageLoad < 1000 -> 60
            else -> 40
        }

        val verdict = when {
            avgImageLoad < 500 && avgVideoPreload < 1500 -> "Smooth scrolling | Images load <1s | Videos preload adequately"
            avgImageLoad < 1000 && avgVideoPreload < 3000 -> "Adequate scrolling | Some image loading delay | Videos may buffer"
            else -> "Slow experience | Images take several seconds | Videos may not preload"
        }

        SocialMediaResult(
            imageLoadTimeMs = avgImageLoad,
            videoPreloadTimeMs = avgVideoPreload,
            scrollSmoothness = smoothness,
            verdict = verdict
        )
    }
}
