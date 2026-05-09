package com.rudra.internetspeedtest.feature.realitycheck

import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class RealityCheckReport(private val context: Context) {
    
    fun generateReport(result: RealityCheckEngine.RealityCheckResult): File {
        val fileName = "RealityCheck_${result.testId}.pdf"
        val file = File(context.cacheDir, fileName)
        
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = document.startPage(pageInfo)
        
        val canvas = page.canvas
        val titlePaint = Paint().apply {
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            color = android.graphics.Color.BLACK
        }
        
        val headerPaint = Paint().apply {
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            color = android.graphics.Color.BLACK
        }
        
        val bodyPaint = Paint().apply {
            textSize = 12f
            color = android.graphics.Color.BLACK
        }
        
        val smallPaint = Paint().apply {
            textSize = 10f
            color = android.graphics.Color.GRAY
        }
        
        var y = 50f
        val leftMargin = 40f
        
        // Title
        canvas.drawText("REALITY CHECK REPORT", leftMargin, y, titlePaint)
        y += 30f
        
        // Test ID and Date
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.getDefault())
        val dateStr = dateFormat.format(Date(result.timestamp))
        
        canvas.drawText("Test ID: ${result.testId}", leftMargin, y, bodyPaint)
        y += 20f
        canvas.drawText("Date: $dateStr", leftMargin, y, bodyPaint)
        y += 40f
        
        // Summary Box
        canvas.drawText("SUMMARY", leftMargin, y, headerPaint)
        y += 25f
        
        val verdict = result.verdict
        val verdictText = "${verdict.emoji} ${verdict.label}"
        canvas.drawText(verdictText, leftMargin, y, headerPaint)
        y += 30f
        
        canvas.drawText("PROMISED: ${result.config.promisedSpeedMbps} Mbps", leftMargin, y, bodyPaint)
        y += 20f
        canvas.drawText("ACTUAL: ${result.averageSpeedMbps} Mbps (${result.achievedPercentOfPromised.roundToInt()}%)", leftMargin, y, bodyPaint)
        y += 40f
        
        // Test Methodology
        canvas.drawText("TEST METHODOLOGY", leftMargin, y, headerPaint)
        y += 25f
        
        val methodology = listOf(
            "• ${result.config.testCount} independent tests run",
            "• ${result.config.testIntervalSeconds} seconds between tests",
            "• Multi-threaded to saturate connection",
            "• Outlier-filtered for accuracy"
        )
        
        methodology.forEach { line ->
            canvas.drawText(line, leftMargin, y, bodyPaint)
            y += 18f
        }
        y += 20f
        
        // Individual Results
        canvas.drawText("INDIVIDUAL RESULTS", leftMargin, y, headerPaint)
        y += 25f
        
        result.individualTests.forEachIndexed { index, test ->
            canvas.drawText(
                "Test ${index + 1}: ${test.downloadSpeedMbps} Mbps ↓ / ${test.uploadSpeedMbps} Mbps ↑",
                leftMargin, y, bodyPaint
            )
            y += 18f
        }
        y += 20f
        
        // Statistics
        canvas.drawText("STATISTICS", leftMargin, y, headerPaint)
        y += 25f
        
        val stats = listOf(
            "Average Speed: ${result.averageSpeedMbps} Mbps",
            "Peak Speed: ${result.peakSpeedMbps} Mbps",
            "Minimum Speed: ${result.minimumSpeedMbps} Mbps",
            "Consistency: ${result.consistencyPercent.roundToInt()}%",
            "Confidence Score: ${result.confidenceScore}/100"
        )
        
        stats.forEach { line ->
            canvas.drawText(line, leftMargin, y, bodyPaint)
            y += 18f
        }
        y += 20f
        
        // Plan Type
        canvas.drawText("PLAN DETAILS", leftMargin, y, headerPaint)
        y += 25f
        
        canvas.drawText("Plan Type: ${result.config.planType.label}", leftMargin, y, bodyPaint)
        y += 18f
        canvas.drawText("Typical Expectation: ${result.config.planType.typicalExpectation}", leftMargin, y, bodyPaint)
        y += 18f
        
        result.config.ispName?.let { ispName ->
            canvas.drawText("ISP: $ispName", leftMargin, y, bodyPaint)
            y += 18f
        }
        y += 30f
        
        // Recommendation
        canvas.drawText("RECOMMENDATION", leftMargin, y, headerPaint)
        y += 25f
        
        // Wrap text for recommendation
        val words = result.recommendation.split(" ")
        var line = ""
        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            if (bodyPaint.measureText(testLine) > 500) {
                canvas.drawText(line, leftMargin, y, bodyPaint)
                y += 16f
                line = word
            } else {
                line = testLine
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line, leftMargin, y, bodyPaint)
        }
        
        document.finishPage(page)
        
        FileOutputStream(file).use { outputStream ->
            document.writeTo(outputStream)
        }
        document.close()
        
        return file
    }
    
    fun shareReport(file: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Reality Check Report - ${file.nameWithoutExtension}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}