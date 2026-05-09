package com.rudra.internetspeedtest.feature.realitycheck

import com.rudra.internetspeedtest.core.testing.BufferbloatGrade
import com.rudra.internetspeedtest.core.testing.ConfidenceCalculator
import com.rudra.internetspeedtest.domain.model.SpeedTestResult
import com.rudra.internetspeedtest.domain.repository.InternetSpeedTestRepository
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Core engine that compares ISP promises against measured reality.
 * 
 * Philosophy: A single speed test is a snapshot. Reality is measured
 * over time, across conditions. This engine runs multiple tests and
 * delivers a verdict based on consistency, not just peak performance.
 */
class RealityCheckEngine(
    private val speedTestEngine: InternetSpeedTestRepository,
    private val confidenceCalculator: ConfidenceCalculator
) {
    data class RealityCheckConfig(
        val promisedSpeedMbps: Double,
        val planType: PlanType,
        val ispName: String?,
        val testCount: Int = 3,              // Run 3 tests for reliability
        val testIntervalSeconds: Int = 30,    // 30 seconds between tests
        val thresholdPercent: Double = 80.0   // Must achieve 80% of promised
    )
    
    enum class PlanType(val label: String, val typicalExpectation: String) {
        HOME_BROADBAND("Home Broadband", "Consistent, close to advertised"),
        MOBILE_DATA("Mobile Data", "Variable, 'up to' advertised"),
        BUSINESS("Business", "Guaranteed minimum, SLA-backed")
    }
    
    data class RealityCheckResult(
        val config: RealityCheckConfig,
        val individualTests: List<SpeedTestResult>,
        val averageSpeedMbps: Double,
        val peakSpeedMbps: Double,
        val minimumSpeedMbps: Double,
        val consistencyPercent: Double,      // How consistent across tests
        val achievedPercentOfPromised: Double,
        val confidenceScore: Int,
        val verdict: RealityVerdict,
        val timestamp: Long,
        val testId: String,
        val recommendation: String
    )
    
    enum class RealityVerdict(val emoji: String, val label: String) {
        EXCEEDING("🎉", "Better than promised"),
        MEETING("✅", "Getting what you pay for"),
        CLOSE("⚠️", "Almost there"),
        FALLING_SHORT("❌", "Not getting promised speed"),
        SEVERELY_SHORT("🚨", "Significantly below promise"),
        DECEPTIVE("💀", "Nowhere near advertised")
    }
    
    /**
     * Runs the full reality check: multiple tests, statistical analysis,
     * and a clear verdict with evidence.
     */
    suspend fun runRealityCheck(config: RealityCheckConfig): RealityCheckResult {
        val results = mutableListOf<SpeedTestResult>()
        
        // Run multiple tests for statistical validity
        repeat(config.testCount) { index ->
            if (index > 0) delay(config.testIntervalSeconds * 1000L)
            
            val result = speedTestEngine.runSpeedTest { /* progress callback - we don't need progress for reality check */ }
            results.add(result)
        }
        
        // Calculate aggregate metrics
        val speeds = results.map { it.downloadSpeedMbps }
        val averageSpeed = speeds.average()
        val peakSpeed = speeds.maxOrNull() ?: 0.0
        val minimumSpeed = speeds.minOrNull() ?: 0.0
        val consistency = calculateConsistency(speeds)
        val achievedPercent = (averageSpeed / config.promisedSpeedMbps) * 100
        
        // Calculate verdict
        val verdict = determineVerdict(achievedPercent, consistency, config.planType)
        
        // Generate recommendation
        val recommendation = generateRecommendation(verdict, config, achievedPercent, consistency)
        
        // Calculate overall confidence
        val confidence = confidenceCalculator.calculate(
            samples = speeds,
            variance = speeds.variance(),
            threadConsistency = consistency / 100.0,
            packetLoss = results.map { it.packetLoss }.average(),
            bufferbloatGrade = BufferbloatGrade.valueOf(results.firstOrNull()?.bufferbloatGrade ?: "B")
        )
        
        return RealityCheckResult(
            config = config,
            individualTests = results,
            averageSpeedMbps = averageSpeed.roundToDecimals(2),
            peakSpeedMbps = peakSpeed.roundToDecimals(2),
            minimumSpeedMbps = minimumSpeed.roundToDecimals(2),
            consistencyPercent = consistency.roundToDecimals(1),
            achievedPercentOfPromised = achievedPercent.roundToDecimals(1),
            confidenceScore = confidence.score,
            verdict = verdict,
            timestamp = System.currentTimeMillis(),
            testId = generateTestId(),
            recommendation = recommendation
        )
    }
    
    /**
     * Consistency = how close individual tests are to each other.
     * High consistency + low speed = ISP is stable but slow (throttling).
     * Low consistency + high average = unstable but fast (congestion).
     */
    private fun calculateConsistency(speeds: List<Double>): Double {
        if (speeds.size < 2) return 100.0
        
        val avg = speeds.average()
        if (avg == 0.0) return 0.0
        
        val deviations = speeds.map { abs(it - avg) / avg }
        val avgDeviation = deviations.average()
        
        return ((1.0 - avgDeviation) * 100).coerceIn(0.0, 100.0)
    }
    
    /**
     * Verdict logic accounts for plan type expectations.
     * Mobile "up to 50 Mbps" getting 35 is different from
     * Fiber "100 Mbps" getting 35.
     */
    private fun determineVerdict(
        achievedPercent: Double,
        consistency: Double,
        planType: PlanType
    ): RealityVerdict {
        return when {
            achievedPercent >= 100 -> RealityVerdict.EXCEEDING
            achievedPercent >= 90 -> RealityVerdict.MEETING
            achievedPercent >= 80 -> RealityVerdict.CLOSE
            achievedPercent >= 60 -> {
                // For mobile "up to" plans, 60% is more acceptable
                if (planType == PlanType.MOBILE_DATA && consistency > 70) {
                    RealityVerdict.CLOSE
                } else {
                    RealityVerdict.FALLING_SHORT
                }
            }
            achievedPercent >= 40 -> RealityVerdict.FALLING_SHORT
            achievedPercent >= 20 -> RealityVerdict.SEVERELY_SHORT
            else -> RealityVerdict.DECEPTIVE
        }
    }
    
    /**
     * Recommendations are actionable and evidence-based.
     * Never emotional. Always helpful.
     */
    private fun generateRecommendation(
        verdict: RealityVerdict,
        config: RealityCheckConfig,
        achievedPercent: Double,
        consistency: Double
    ): String {
        val promised = config.promisedSpeedMbps.roundToDecimals(0)
        
        return when (verdict) {
            RealityVerdict.EXCEEDING -> 
                "You're getting more than the ${promised} Mbps promised. " +
                "Your connection is performing excellently."
            
            RealityVerdict.MEETING -> 
                "You're receiving ${achievedPercent.roundToInt()}% of your promised ${promised} Mbps. " +
                "This is within normal expectations."
            
            RealityVerdict.CLOSE -> {
                if (consistency > 80) {
                    "You receive ${achievedPercent.roundToInt()}% of promised speed, but results are consistent. " +
                    "This may indicate a line limitation rather than congestion. " +
                    "Contact your ISP to verify your line can support ${promised} Mbps."
                } else {
                    "You receive ${achievedPercent.roundToInt()}% of promised speed with inconsistent results. " +
                    "This suggests network congestion. Try testing at different times of day."
                }
            }
            
            RealityVerdict.FALLING_SHORT -> 
                "You're receiving only ${achievedPercent.roundToInt()}% of your promised ${promised} Mbps. " +
                "We recommend contacting your ISP with these test results. " +
                "You can export a detailed report from this screen."
            
            RealityVerdict.SEVERELY_SHORT -> 
                "Your connection delivers only ${achievedPercent.roundToInt()}% of advertised speed. " +
                "This is a significant shortfall. Export the full report and contact your ISP. " +
                "If unresolved, consider filing a complaint with your telecommunications regulator."
            
            RealityVerdict.DECEPTIVE -> 
                "Warning: Your connection achieves barely ${achievedPercent.roundToInt()}% of the ${promised} Mbps " +
                "you're paying for. This is not normal under any plan type. " +
                "Export your evidence report immediately and escalate to your ISP and regulator."
        }
    }
    
    private fun generateTestId(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return "RC-" + (1..6).map { chars.random() }.joinToString("")
    }
    
    private fun Double.roundToDecimals(decimals: Int): Double {
        val factor = Math.pow(10.0, decimals.toDouble())
        return ((this * factor).toLong()).toDouble() / factor
    }
    
    // Extension to calculate variance
    private fun List<Double>.variance(): Double {
        if (size < 2) return 0.0
        val avg = average()
        return map { (it - avg) * (it - avg) }.average()
    }
}