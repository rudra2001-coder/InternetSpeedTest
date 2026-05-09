package com.rudra.internetspeedtest.feature.neutrality

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

data class NeutralityResult(
    val baselineSpeed: Double,
    val serviceResults: List<ServiceVariance>,
    val score: NeutralityScore,
    val report: String,
    val timestamp: Long = System.currentTimeMillis()
)

class NeutralityReportExporter {
    fun exportPDF(context: Context, result: NeutralityResult): File {
        val dir = File(context.cacheDir, "reports")
        dir.mkdirs()
        val file = File(dir, "neutrality_report_${result.timestamp}.txt")

        file.bufferedWriter().use { writer ->
            writer.write("=== Network Consistency Report ===\n")
            writer.write("Generated: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(result.timestamp)}\n")
            writer.write("Baseline Speed: ${String.format("%.1f", result.baselineSpeed)} Mbps\n")
            writer.write("Neutrality Score: ${result.score.score}/100\n")
            writer.write("Pattern: ${result.score.pattern}\n\n")
            writer.write("--- Methodology ---\n")
            writer.write("We tested multiple services and compared their speeds to a baseline control endpoint. ")
            writer.write("Scores reflect consistency across all tested services.\n\n")
            writer.write("--- Results ---\n")
            result.serviceResults.forEach { v ->
                val direction = if (v.deviationPercent > 0) "+" else ""
                writer.write("${v.service}: ${String.format("%.1f", v.serviceSpeed)} Mbps ($direction${String.format("%.1f", v.deviationPercent)}% vs baseline)\n")
            }
            writer.write("\n--- Analysis ---\n")
            writer.write(result.report)
        }
        return file
    }

    fun shareReport(context: Context, result: NeutralityResult) {
        val file = exportPDF(context, result)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Report"))
    }
}
