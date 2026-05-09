package com.rudra.internetspeedtest.feature.realitycheck

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun RealityVerdictCard(result: RealityCheckEngine.RealityCheckResult) {
    val verdict = result.verdict
    
    val (backgroundColor, textColor, emoji) = when (verdict) {
        RealityCheckEngine.RealityVerdict.EXCEEDING -> Triple(Color(0xFF1B5E20), Color.White, "🎉")
        RealityCheckEngine.RealityVerdict.MEETING -> Triple(Color(0xFF2E7D32), Color.White, "✅")
        RealityCheckEngine.RealityVerdict.CLOSE -> Triple(Color(0xFFF57F17), Color.White, "⚠️")
        RealityCheckEngine.RealityVerdict.FALLING_SHORT -> Triple(Color(0xFFE65100), Color.White, "❌")
        RealityCheckEngine.RealityVerdict.SEVERELY_SHORT -> Triple(Color(0xFFC62828), Color.White, "🚨")
        RealityCheckEngine.RealityVerdict.DECEPTIVE -> Triple(Color(0xFF880E4F), Color.White, "💀")
    }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "$emoji ${verdict.label}",
                style = MaterialTheme.typography.headlineMedium,
                color = textColor
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${result.averageSpeedMbps}",
                    style = MaterialTheme.typography.displaySmall,
                    color = textColor
                )
                Text(
                    text = " Mbps",
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor.copy(alpha = 0.8f)
                )
            }
            
            Text(
                text = "of ${result.config.promisedSpeedMbps} Mbps promised",
                style = MaterialTheme.typography.bodyLarge,
                color = textColor.copy(alpha = 0.9f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { (result.achievedPercentOfPromised / 100).coerceIn(0.0, 1.0).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = textColor,
                trackColor = textColor.copy(alpha = 0.3f),
            )
            
            Text(
                text = "${result.achievedPercentOfPromised.roundToInt()}% of promised speed",
                style = MaterialTheme.typography.labelMedium,
                color = textColor.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = result.recommendation,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
        }
    }
}

@Composable
fun RealityEvidenceCard(result: RealityCheckEngine.RealityCheckResult) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "How We Determined This",
                style = MaterialTheme.typography.titleSmall
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            EvidenceRow(
                label = "Tests Run",
                value = "${result.config.testCount} tests, ${result.config.testIntervalSeconds}s apart"
            )
            
            EvidenceRow(
                label = "Average Speed",
                value = "${result.averageSpeedMbps} Mbps"
            )
            
            EvidenceRow(
                label = "Speed Range",
                value = "${result.minimumSpeedMbps} - ${result.peakSpeedMbps} Mbps"
            )
            
            EvidenceRow(
                label = "Consistency",
                value = "${result.consistencyPercent.roundToInt()}%",
                warning = result.consistencyPercent < 70
            )
            
            EvidenceRow(
                label = "Confidence",
                value = "${result.confidenceScore}/100"
            )
            
            EvidenceRow(
                label = "Plan Type",
                value = result.config.planType.label
            )
            
            result.config.ispName?.let { ispName ->
                EvidenceRow(
                    label = "ISP",
                    value = ispName
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Individual Tests:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            result.individualTests.forEachIndexed { index, test ->
                Text(
                    text = "  Test ${index + 1}: ${test.downloadSpeedMbps} Mbps ↓ / ${test.uploadSpeedMbps} Mbps ↑",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun EvidenceRow(
    label: String,
    value: String,
    warning: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = if (warning) Color(0xFFE65100) 
                    else MaterialTheme.colorScheme.onSurface
        )
    }
}