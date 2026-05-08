package com.rudra.internetspeedtest.data.neutrality

import com.rudra.internetspeedtest.domain.model.NeutralityReport
import com.rudra.internetspeedtest.domain.model.ServiceTestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NeutralityCheckEngine @Inject constructor() {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
    
    private val serviceEndpoints = listOf(
        ServiceEndpoint("YouTube", "https://redirector.googlevideo.com"),
        ServiceEndpoint("Netflix", "https://fast.com"),
        ServiceEndpoint("Facebook", "https://graph.facebook.com"),
        ServiceEndpoint("Control (Cloudflare)", "https://speed.cloudflare.com/__down")
    )
    
    suspend fun runNeutralityCheck(baselineSpeed: Double): NeutralityReport = withContext(Dispatchers.IO) {
        val results = mutableListOf<ServiceTestResult>()
        
        for (service in serviceEndpoints) {
            val speed = testServiceSpeed(service.endpoint)
            val deviation = if (baselineSpeed > 0) {
                ((speed - baselineSpeed) / baselineSpeed) * 100
            } else 0.0
            
            val status = when {
                deviation > 30 -> "elevated"
                deviation < -30 -> "reduced"
                else -> "normal"
            }
            
            results.add(
                ServiceTestResult(
                    serviceName = service.name,
                    endpoint = service.endpoint,
                    speedMbps = speed,
                    deviationPercent = deviation,
                    status = status
                )
            )
        }
        
        val controlResult = results.find { it.serviceName.contains("Control") }
        val otherResults = results.filter { !it.serviceName.contains("Control") }
        
        val avgDeviation = otherResults.map { kotlin.math.abs(it.deviationPercent) }.averageOrNull() ?: 0.0
        val neutralityScore = (100 - avgDeviation).coerceIn(0.0, 100.0).toInt()
        val variationDetected = otherResults.any { kotlin.math.abs(it.deviationPercent) > 30 }
        
        val summary = buildSummary(otherResults, controlResult)
        val recommendation = buildRecommendation(otherResults)
        
        NeutralityReport(
            baselineSpeedMbps = baselineSpeed,
            serviceResults = results,
            neutralityScore = neutralityScore,
            variationDetected = variationDetected,
            summary = summary,
            recommendation = recommendation
        )
    }
    
    private suspend fun testServiceSpeed(endpoint: String): Double = withContext(Dispatchers.IO) {
        return@withContext try {
            val request = Request.Builder().url(endpoint).build()
            val startTime = System.nanoTime()
            val response = client.newCall(request).execute()
            response.close()
            val endTime = System.nanoTime()
            val durationSec = (endTime - startTime) / 1_000_000_000.0
            if (durationSec <= 0) 0.0 else 1.0 / durationSec
        } catch (e: Exception) { 0.0 }
    }
    
    private fun buildSummary(results: List<ServiceTestResult>, control: ServiceTestResult?): String {
        val variations = results.filter { kotlin.math.abs(it.deviationPercent) > 30 }
        return if (variations.isEmpty()) {
            "No significant variation detected across services."
        } else {
            "Performance variation detected: " + variations.joinToString { "${it.serviceName} (${String.format("%.0f", it.deviationPercent)}%)" }
        }
    }
    
    private fun buildRecommendation(results: List<ServiceTestResult>): String {
        val hasSignificant = results.any { kotlin.math.abs(it.deviationPercent) > 50 }
        return if (hasSignificant) {
            "Significant variation detected. This may indicate traffic prioritization, CDN routing differences, or current server load. Run a Real-Use Test to verify."
        } else if (results.any { kotlin.math.abs(it.deviationPercent) > 30 }) {
            "Moderate variation detected. Consider running a Real-Use Test for streaming services."
        } else {
            "Network performance appears consistent across services."
        }
    }
}

data class ServiceEndpoint(val name: String, val endpoint: String)

fun List<Double>.averageOrNull(): Double? {
    if (isEmpty()) return null
    return average()
}
