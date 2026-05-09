package com.rudra.internetspeedtest.feature.realuse

data class RealUseReport(
    val streamingResult: StreamingSimResult?,
    val callQuality: CallQualityResult?,
    val gamingResult: GamingResult?,
    val socialMediaResult: SocialMediaResult?,
    val downloadPrediction: DownloadPrediction?,
    val householdCapacity: HouseholdCapacity?
) {
    fun summary(): String = buildString {
        streamingResult?.let {
            appendLine("Streaming: ${it.stableResolution.label} stable | Buffer: ${it.bufferHealthPercent}% | Stalls: ${it.stallEvents}")
        }
        callQuality?.let {
            appendLine("Video Calls: ${it.resolution.label} | MOS: ${String.format("%.1f", it.mosScore)}/5 | Jitter: ${String.format("%.1f", it.jitterMs)}ms")
        }
        gamingResult?.let {
            appendLine("Gaming: ${it.playabilityVerdict.label} | ${String.format("%.0f", it.averagePingMs)}ms avg | ${it.pingSpikes} spikes")
        }
        socialMediaResult?.let {
            appendLine("Social: ${it.verdict}")
        }
        householdCapacity?.let {
            appendLine("Household: ${it.recommendation}")
        }
    }
}

class RealUseReportGenerator {
    fun generate(
        streamingResult: StreamingSimResult? = null,
        callQuality: CallQualityResult? = null,
        gamingResult: GamingResult? = null,
        socialMediaResult: SocialMediaResult? = null,
        downloadPrediction: DownloadPrediction? = null,
        householdCapacity: HouseholdCapacity? = null
    ): RealUseReport {
        return RealUseReport(
            streamingResult = streamingResult,
            callQuality = callQuality,
            gamingResult = gamingResult,
            socialMediaResult = socialMediaResult,
            downloadPrediction = downloadPrediction,
            householdCapacity = householdCapacity
        )
    }
}
