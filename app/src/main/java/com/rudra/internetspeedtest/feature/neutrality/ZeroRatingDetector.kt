package com.rudra.internetspeedtest.feature.neutrality

data class ZeroRatingAssessment(
    val pattern: String,
    val confidence: Int
)

class ZeroRatingDetector {
    fun detect(serviceVariances: List<ServiceVariance>): ZeroRatingAssessment {
        val streamingServices = serviceVariances.filter {
            it.service in listOf("YouTube", "Netflix")
        }
        val socialServices = serviceVariances.filter {
            it.service in listOf("Facebook", "Instagram")
        }

        if (streamingServices.isEmpty() && socialServices.isEmpty()) {
            return ZeroRatingAssessment("Insufficient data to detect prioritization patterns", confidence = 50)
        }

        val streamingAvg = if (streamingServices.isNotEmpty()) streamingServices.map { it.deviationPercent }.average() else 0.0
        val socialAvg = if (socialServices.isNotEmpty()) socialServices.map { it.deviationPercent }.average() else 0.0

        return when {
            streamingAvg > 50 && socialAvg < -20 ->
                ZeroRatingAssessment("Video streaming services appear prioritized", confidence = 60)
            socialAvg > 50 && streamingAvg < -20 ->
                ZeroRatingAssessment("Social media services appear prioritized", confidence = 60)
            else ->
                ZeroRatingAssessment("No clear prioritization pattern detected", confidence = 80)
        }
    }
}
