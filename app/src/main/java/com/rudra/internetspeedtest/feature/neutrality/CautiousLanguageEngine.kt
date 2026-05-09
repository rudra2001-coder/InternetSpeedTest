package com.rudra.internetspeedtest.feature.neutrality

import kotlin.math.abs
import kotlin.math.roundToInt

class CautiousLanguageEngine {

    private val phraseReplacements = mapOf(
        "no throttling detected" to "no differential treatment detected",
        "no manipulation detected" to "consistent treatment detected",
        "throttling policy" to "traffic management policy",
        "ISP is throttling" to "ISP appears to be rate-limiting",
        "manipulating speeds" to "applying traffic optimization",
        "throttling your" to "rate-limiting your",
        "blocking access" to "restricting access",
        "cheating users" to "optimizing traffic",
        "discriminating against" to "differentiating between",
        "violating neutrality" to "impacting consistency"
    )

    private val bannedWords = mapOf(
        Regex("throttling|throttle", RegexOption.IGNORE_CASE) to "rate-limiting",
        Regex("blocking|blocked|block", RegexOption.IGNORE_CASE) to "restricting",
        Regex("manipulating|manipulate|manipulated", RegexOption.IGNORE_CASE) to "influencing",
        Regex("violating|violate|violation", RegexOption.IGNORE_CASE) to "impacting",
        Regex("discriminating|discriminate|discrimination", RegexOption.IGNORE_CASE) to "differentiating",
        Regex("cheating|cheat|cheated", RegexOption.IGNORE_CASE) to "optimizing"
    )

    fun generateReport(score: NeutralityScore, variance: List<ServiceVariance>): String {
        val builder = StringBuilder()

        when {
            score.score >= 90 -> {
                builder.append("Services tested show minimal performance variation. ")
                builder.append("Your connection appears to treat all services consistently.")
            }
            score.score >= 70 -> {
                builder.append("Moderate performance variation detected across services. ")
                builder.append("This may be due to CDN routing differences, server location, ")
                builder.append("or network traffic prioritization.")
            }
            score.score >= 50 -> {
                builder.append("Significant performance variation detected. ")
                builder.append("Some services show notably different speeds than our baseline. ")
                builder.append("Consider running a Real-Use Test to verify actual experience.")
            }
            else -> {
                builder.append("Substantial performance differences detected between services. ")
                builder.append("This pattern may indicate traffic management policies. ")
                builder.append("Results are not conclusive—network conditions vary over time.")
            }
        }

        variance.filter { it.deviationPercent > 20 }.forEach { v ->
            builder.append(" ${v.service} tested faster than baseline (+${v.deviationPercent.roundToInt()}%). ")
        }
        variance.filter { it.deviationPercent < -20 }.forEach { v ->
            builder.append(" ${v.service} tested slower than baseline (${v.deviationPercent.roundToInt()}%). ")
        }

        return sanitize(builder.toString().trim())
    }

    private fun sanitize(text: String): String {
        var result = text.lowercase()

        for ((phrase, replacement) in phraseReplacements) {
            result = result.replace(phrase, replacement, ignoreCase = true)
        }

        for ((pattern, replacement) in bannedWords) {
            result = pattern.replace(result, replacement)
        }

        return result.replaceFirstChar { it.uppercase() }
    }
}
