package com.rudra.internetspeedtest.core.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class SpeedTestServer(
    val name: String,
    val url: String,
    val priority: Int = Int.MAX_VALUE
)

data class SelectedServer(
    val server: SpeedTestServer,
    val latencyMs: Double
)

@Singleton
class ServerPoolManager @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private val servers = listOf(
        SpeedTestServer("Cloudflare", "https://speed.cloudflare.com/__down", priority = 1),
        SpeedTestServer("Tele2", "https://speedtest.tele2.net/10MB.zip", priority = 2),
        SpeedTestServer("Google-CDN", "https://speed.googlefiber.net/__down", priority = 3),
        SpeedTestServer("Linode", "https://speedtest.newark.linode.com/100MB-newark.bin", priority = 4)
    )

    suspend fun selectOptimal(count: Int = 3): List<SelectedServer> = withContext(Dispatchers.IO) {
        val results = servers.map { server ->
            async {
                val latency = measureLatency(server.url)
                SelectedServer(server, latency)
            }
        }.awaitAll()

        results
            .filter { it.latencyMs > 0 }
            .sortedBy { it.latencyMs }
            .take(count)
    }

    suspend fun selectDefault(): List<SpeedTestServer> = servers.sortedBy { it.priority }

    private suspend fun measureLatency(url: String): Double = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).head().build()
            val start = System.nanoTime()
            val response = client.newCall(request).execute()
            response.close()
            (System.nanoTime() - start) / 1_000_000.0
        } catch (_: Exception) { -1.0 }
    }
}
