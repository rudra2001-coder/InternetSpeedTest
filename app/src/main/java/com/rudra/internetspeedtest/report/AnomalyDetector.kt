package com.rudra.internetspeedtest.report

import com.rudra.internetspeedtest.domain.model.SpeedTestResult
import java.util.Calendar

data class Anomaly(
    val type: AnomalyType,
    val description: String,
    val severity: AnomalySeverity
)

enum class AnomalyType { SPEED_DROP, UPLOAD_DEGRADATION, HIGH_VARIANCE, TIME_OF_DAY_PATTERN }
enum class AnomalySeverity { INFO, WARNING, CRITICAL }

class AnomalyDetector {
    fun detectAnomalies(current: SpeedTestResult, history: List<SpeedTestResult>): List<Anomaly> {
        val anomalies = mutableListOf<Anomaly>()

        if (history.isEmpty()) return anomalies

        val avgHistorySpeed = history.map { it.downloadSpeedMbps }.average()

        if (current.downloadSpeedMbps < avgHistorySpeed * 0.5 && avgHistorySpeed > 0) {
            val dropPercent = ((1 - current.downloadSpeedMbps / avgHistorySpeed) * 100).toInt()
            anomalies.add(
                Anomaly(
                    type = AnomalyType.SPEED_DROP,
                    description = "Speed dropped ${dropPercent}% compared to your average (${String.format("%.0f", avgHistorySpeed)} Mbps)",
                    severity = if (dropPercent > 70) AnomalySeverity.CRITICAL else AnomalySeverity.WARNING
                )
            )
        }

        val avgUpload = history.map { it.uploadSpeedMbps }.average()
        if (current.uploadSpeedMbps < avgUpload * 0.5 && avgUpload > 0) {
            anomalies.add(
                Anomaly(
                    type = AnomalyType.UPLOAD_DEGRADATION,
                    description = "Upload speed significantly lower than usual",
                    severity = AnomalySeverity.WARNING
                )
            )
        }

        if (current.confidenceScore < 40) {
            anomalies.add(
                Anomaly(
                    type = AnomalyType.HIGH_VARIANCE,
                    description = "Test results show unusually high variance (confidence: ${current.confidenceScore}/100)",
                    severity = AnomalySeverity.INFO
                )
            )
        }

        val hourAnomalies = detectTimeOfDayPatterns(current, history)
        anomalies.addAll(hourAnomalies)

        return anomalies
    }

    private fun detectTimeOfDayPatterns(current: SpeedTestResult, history: List<SpeedTestResult>): List<Anomaly> {
        val anomalies = mutableListOf<Anomaly>()
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        val eveningTests = history.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            cal.get(Calendar.HOUR_OF_DAY) in 20..23
        }
        if (eveningTests.size >= 2) {
            val eveningAvg = eveningTests.map { it.downloadSpeedMbps }.average()
            val overallAvg = history.filter { it.timestamp != current.timestamp }
                .map { it.downloadSpeedMbps }.average()
            if (eveningAvg < overallAvg * 0.7 && overallAvg > 0) {
                val drop = ((1 - eveningAvg / overallAvg) * 100).toInt()
                anomalies.add(
                    Anomaly(
                        type = AnomalyType.TIME_OF_DAY_PATTERN,
                        description = "Speed drops approximately ${drop}% during evening hours (8-11 PM)",
                        severity = AnomalySeverity.WARNING
                    )
                )
            }
        }
        return anomalies
    }
}
