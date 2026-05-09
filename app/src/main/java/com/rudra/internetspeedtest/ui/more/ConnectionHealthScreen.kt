package com.rudra.internetspeedtest.ui.more

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.internetspeedtest.report.Anomaly
import com.rudra.internetspeedtest.report.AnomalySeverity
import com.rudra.internetspeedtest.report.HealthScore
import com.rudra.internetspeedtest.theme.Primary
import com.rudra.internetspeedtest.theme.Success
import com.rudra.internetspeedtest.theme.Surface
import com.rudra.internetspeedtest.theme.SurfaceVariant
import com.rudra.internetspeedtest.theme.Warning
import com.rudra.internetspeedtest.ui.speedtest.SpeedTestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionHealthScreen(
    onNavigateBack: () -> Unit,
    viewModel: SpeedTestViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connection Health") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (uiState.result == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Run a speed test first", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("Complete a speed test to see your connection health score, anomalies, and ISP report card.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                return@Column
            }

            val result = uiState.result!!

            if (uiState.healthScore != null) {
                HealthScoreDetailCard(uiState.healthScore!!)
            }

            Spacer(Modifier.height(16.dp))

            if (uiState.anomalies.isNotEmpty()) {
                AnomaliesDetailCard(uiState.anomalies)
            }

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Test Details", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    DetailRow("Download", "${String.format("%.1f", result.downloadSpeedMbps)} Mbps")
                    DetailRow("Upload", "${String.format("%.1f", result.uploadSpeedMbps)} Mbps")
                    DetailRow("Ping", "${String.format("%.0f", result.pingMs)} ms")
                    DetailRow("Jitter", "${String.format("%.1f", result.jitterMs)} ms")
                    DetailRow("Bufferbloat", result.bufferbloatGrade)
                    DetailRow("Confidence", "${result.confidenceScore}/100")
                    result.connectionContext?.let { ctx ->
                        Spacer(Modifier.height(4.dp))
                        Text(ctx.displayString, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun HealthScoreDetailCard(health: HealthScore) {
    val color = when {
        health.score >= 75 -> Success
        health.score >= 55 -> Warning
        else -> Color.Red
    }
    val gradeColor = when (health.grade) {
        "Excellent" -> Success
        "Good" -> Success
        "Fair" -> Warning
        "Poor", "Critical" -> Color.Red
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Favorite, null, Modifier.size(28.dp), tint = color)
                Spacer(Modifier.width(8.dp))
                Text("Connection Health", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            Box(Modifier.size(100.dp).background(color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${health.score}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = color)
                    Text("/100", style = MaterialTheme.typography.bodySmall, color = color)
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("Grade: ${health.grade}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = gradeColor)

            Spacer(Modifier.height(16.dp))

            LinearProgressIndicator(progress = { health.score / 100f }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = color, trackColor = color.copy(alpha = 0.2f))

            Spacer(Modifier.height(16.dp))

            BreakdownRow("Speed Adequacy", health.speedAdequacy, 25)
            BreakdownRow("Stability", health.stabilityScore, 25)
            BreakdownRow("Bufferbloat", health.bufferbloatScore, 20)
            BreakdownRow("Neutrality", health.neutralityScore, 15)
            BreakdownRow("Real-Use", health.realUseScore, 15)
        }
    }
}

@Composable
fun BreakdownRow(label: String, score: Int, max: Int) {
    val ratio = score.toFloat() / max.coerceAtLeast(1)
    val color = when {
        ratio >= 0.7 -> Success
        ratio >= 0.4 -> Warning
        else -> Color.Red
    }
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text("$score/$max", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
fun AnomaliesDetailCard(anomalies: List<Anomaly>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Warning.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Anomalies Detected", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            anomalies.forEach { anomaly ->
                val iconColor = when (anomaly.severity) {
                    AnomalySeverity.CRITICAL -> Color.Red
                    AnomalySeverity.WARNING -> Warning
                    AnomalySeverity.INFO -> Primary
                }
                Row(Modifier.padding(vertical = 6.dp)) {
                    Box(Modifier.size(8.dp).background(iconColor, CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text(anomaly.description, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
