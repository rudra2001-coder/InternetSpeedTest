package com.rudra.internetspeedtest.feature.neutrality

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class ServiceSpeedResult(
    val service: String,
    val speedMbps: Double,
    val sampleCount: Int,
    val variance: Double
)

@Singleton
class ServiceSpeedTester @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun testService(endpoint: ServiceEndpoint): ServiceSpeedResult = withContext(Dispatchers.IO) {
        val samples = mutableListOf<Double>()

        repeat(3) {
            try {
                val request = Request.Builder().url(endpoint.endpoint).build()
                val start = System.nanoTime()
                val response = client.newCall(request).execute()
                val bytes = response.body?.bytes()
                response.close()
                val duration = (System.nanoTime() - start) / 1_000_000_000.0
                if (duration > 0 && bytes != null) {
                    val speed = (bytes.size * 8.0) / duration / 1_000_000
                    samples.add(speed)
                }
            } catch (_: Exception) { }
        }

        val avgSpeed = if (samples.isNotEmpty()) samples.average() else 0.0
        val variance = if (samples.size > 1) samples.map { (it - avgSpeed) * (it - avgSpeed) }.average() else 0.0

        ServiceSpeedResult(
            service = endpoint.service,
            speedMbps = avgSpeed,
            sampleCount = samples.size,
            variance = variance
        )
    }
}
