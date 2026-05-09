package com.rudra.internetspeedtest.feature.realuse

import com.rudra.internetspeedtest.domain.model.SpeedTestResult
import kotlin.math.sqrt

enum class FileSize(val label: String, val mb: Double) {
    SMALL_FILE("10 MB", 10.0),
    MEDIUM_FILE("100 MB", 100.0),
    LARGE_FILE("1 GB", 1024.0),
    GAME_FILE("50 GB", 51200.0),
    UHD_MOVIE("100 GB", 102400.0)
}

data class DownloadPrediction(
    val fileSize: FileSize,
    val estimatedTimeSeconds: Int,
    val estimatedTimeFormatted: String,
    val stabilityWarning: String?
)

class FileDownloadPredictor {
    fun predict(fileSizeMB: Double, speedResult: SpeedTestResult): DownloadPrediction {
        val speedMbps = speedResult.downloadSpeedMbps
        val confidence = speedResult.confidenceScore
        val variance = speedResult.speedSamples.let { samples ->
            if (samples.size > 1) {
                val avg = samples.average()
                samples.map { (it - avg) * (it - avg) }.average()
            } else 0.0
        }

        val rawTimeSeconds = (fileSizeMB * 8.0 / speedMbps.coerceAtLeast(0.1)).toInt()

        val varianceFactor = if (confidence < 50) 1.5 else if (confidence < 70) 1.2 else 1.0
        val adjustedTime = (rawTimeSeconds * varianceFactor).coerceAtLeast(1.0).toInt()

        val varianceRange = (adjustedTime * (sqrt(variance) / speedMbps.coerceAtLeast(0.1)) * 0.3).toInt().coerceAtLeast(1)

        val formatted = if (adjustedTime > 3600) {
            "${adjustedTime / 3600}h ${(adjustedTime % 3600) / 60}m"
        } else if (adjustedTime > 60) {
            "${adjustedTime / 60}m ${adjustedTime % 60}s"
        } else {
            "${adjustedTime}s"
        }

        val rangeFormatted = if (varianceRange > 0) {
            val low = (adjustedTime - varianceRange).coerceAtLeast(1)
            val high = adjustedTime + varianceRange
            val lowStr = if (low > 60) "${low / 60}m" else "${low}s"
            val highStr = if (high > 60) "${high / 60}m" else "${high}s"
            "$lowStr-$highStr"
        } else formatted

        val warning = when {
            confidence < 40 -> "Low confidence: estimate may be inaccurate"
            variance > 50 -> "Connection unstable: actual time may vary significantly"
            else -> null
        }

        val fileSizeEnum = FileSize.entries.minByOrNull { kotlin.math.abs(it.mb - fileSizeMB) } ?: FileSize.LARGE_FILE

        return DownloadPrediction(
            fileSize = fileSizeEnum,
            estimatedTimeSeconds = adjustedTime,
            estimatedTimeFormatted = rangeFormatted,
            stabilityWarning = warning
        )
    }
}
