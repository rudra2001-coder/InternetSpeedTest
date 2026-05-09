package com.rudra.internetspeedtest.ui.speedtest

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.internetspeedtest.core.ui.MethodologyData
import com.rudra.internetspeedtest.domain.model.SpeedTestResult
import com.rudra.internetspeedtest.domain.model.TestStatus
import com.rudra.internetspeedtest.theme.Primary
import com.rudra.internetspeedtest.theme.Success
import com.rudra.internetspeedtest.theme.Surface
import com.rudra.internetspeedtest.theme.SurfaceVariant
import com.rudra.internetspeedtest.theme.Warning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedTestScreen(
    viewModel: SpeedTestViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showMethodology by remember { mutableStateOf(false) }
    var showAuditLog by remember { mutableStateOf(false) }

    if (showMethodology) {
        ModalBottomSheet(
            onDismissRequest = { showMethodology = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            MethodologyContent()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (uiState.result == null) {
            item { SpeedTestHeroSection(uiState) }

            if (uiState.isTestRunning) {
                item { TestProcessCard(uiState) }
                item { SpeedGraphCard(speedSamples = uiState.speedHistory) }
            } else {
                item { SpeedTestInfoCard { showMethodology = true } }
            }

            if (!uiState.isTestRunning) {
                item { StartSpeedTestButton(onClick = { viewModel.startTest() }) }
            }

            if (uiState.errorMessage != null) {
                item { ErrorCard(uiState.errorMessage!!) }
            }

        } else {
            val result = uiState.result!!

            item { MainResultCard(result) }

            item {
                ExpandableSection("How We Got This Result") {
                    TestMethodologySummary(result)
                }
            }

            item {
                ExpandableSection("Confidence: ${result.confidenceScore}/100") {
                    ConfidenceBreakdownContent(result)
                }
            }

            item { BufferbloatCard(result) }

            if (uiState.healthScore != null) {
                item { HealthScoreCard(uiState.healthScore!!) }
            } else {
                item { MissingResultCard("Health score calculation unavailable") }
            }

            if (uiState.anomalies.isNotEmpty()) {
                item { AnomaliesCard(uiState.anomalies) }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showMethodology = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariant)
                    ) { Icon(Icons.Default.Info, null); Spacer(Modifier.width(4.dp)); Text("How We Test") }
                    Button(
                        onClick = { showAuditLog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariant)
                    ) { Icon(Icons.Default.Description, null); Spacer(Modifier.width(4.dp)); Text("Audit Log") }
                }
            }

            if (!uiState.isNeutralityRunning && uiState.neutralityReport == null) {
                item {
                    Button(
                        onClick = { viewModel.runNeutralityCheck() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Warning.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.Default.Info, null, tint = Warning)
                        Spacer(Modifier.width(8.dp))
                        Text("Check Network Consistency", color = Warning)
                    }
                }
            }

            if (uiState.isNeutralityRunning) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Surface)) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Checking network consistency...")
                        }
                    }
                }
            }

            if (uiState.neutralityReport != null) {
                item { NeutralityResultCard(uiState) }
            } else if (!uiState.isNeutralityRunning && !uiState.showNeutralityCheck) {
                item { MissingResultCard("Network consistency check not yet run") }
            }

            if (!uiState.isRealUseRunning && uiState.streamingResult == null) {
                item {
                    Button(
                        onClick = { viewModel.runRealUseTests() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Icon(Icons.Default.PlayArrow, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Run Real-Use Tests")
                    }
                }
            }

            if (uiState.isRealUseRunning) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Surface)) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Running real-world simulations...")
                                Text("Testing streaming, calls, gaming, and more", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            if (uiState.streamingResult != null) {
                item { RealUseResultsSection(uiState) }
            } else if (!uiState.isRealUseRunning && uiState.showRealUseScreen) {
                item { MissingResultCard("Real-use test results unavailable - some simulations could not complete") }
            }

            item {
                Button(
                    onClick = { viewModel.resetTest() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Replay, null)
                    Spacer(Modifier.width(8.dp))
                    Text("New Test")
                }
            }

            if (result.testProvenance != null) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = SurfaceVariant), shape = RoundedCornerShape(12.dp)) {
                        Text(
                            text = result.testProvenance.displayString,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MethodologyContent() {
    Column(Modifier.padding(24.dp)) {
        Text("How We Achieve Accurate Results", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        MethodologyData.steps.forEach { step ->
            Row(Modifier.padding(vertical = 8.dp)) {
                Box(
                    Modifier.size(32.dp).clip(CircleShape).background(Primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) { Text(step.number, color = Primary, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(step.title, fontWeight = FontWeight.SemiBold)
                    Text(step.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun SpeedTestHeroSection(uiState: SpeedTestUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(colors = listOf(Primary, Primary.copy(alpha = 0.7f))),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isTestRunning) {
                        CircularProgressIndicator(Modifier.size(48.dp), color = Color.White, strokeWidth = 4.dp)
                    } else {
                        Icon(Icons.Default.Speed, null, Modifier.size(40.dp), tint = Color.White)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Speed Test", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(4.dp))
                Text(
                    when {
                        uiState.isTestRunning -> uiState.progress.phase.label
                        else -> "Test your real internet speed"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                if (uiState.progress.connectionType.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(uiState.progress.connectionType, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
fun TestProcessCard(uiState: SpeedTestUiState) {
    val p = uiState.progress
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Testing Progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            p.selectedServers.takeIf { it.isNotEmpty() }?.let { servers ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Wifi, null, Modifier.size(16.dp), tint = Primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Servers: ${servers.joinToString()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(8.dp))
            }

            p.threadConfig.takeIf { it.isNotEmpty() }?.let { config ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Speed, null, Modifier.size(16.dp), tint = Primary)
                    Spacer(Modifier.width(8.dp))
                    Text(config, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(8.dp))
            }

            LinearProgressIndicator(
                progress = { p.progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Primary,
                trackColor = Primary.copy(alpha = 0.2f)
            )
            Spacer(Modifier.height(8.dp))

            if (p.currentSpeed > 0) {
                Text("Current: ${String.format("%.1f", p.currentSpeed)} Mbps", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Primary)
            }
            Text(p.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun MainResultCard(result: SpeedTestResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().background(
                brush = Brush.linearGradient(colors = listOf(Primary, Primary.copy(alpha = 0.8f))),
                shape = RoundedCornerShape(24.dp)
            ).padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Test Results", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(24.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    ResultItem("Download", String.format("%.1f", result.downloadSpeedMbps), "Mbps")
                    ResultItem("Upload", String.format("%.1f", result.uploadSpeedMbps), "Mbps")
                }

                Spacer(Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    ResultItem("Ping", String.format("%.0f", result.pingMs), "ms")
                    if (result.loadedDownloadPingMs > 0) {
                        ResultItem("Loaded Ping", String.format("%.0f", result.loadedDownloadPingMs), "ms")
                    }
                    ResultItem("Jitter", String.format("%.1f", result.jitterMs), "ms")
                    ResultItem("Bufferbloat", result.bufferbloatGrade, "")
                }

                if (result.connectionType.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text("Connection: ${result.connectionType} | ISP: ${result.ispName}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                }

                if (result.confidenceScore > 0) {
                    Spacer(Modifier.height(8.dp))
                    Text("Confidence: ${result.confidenceScore}%", style = MaterialTheme.typography.bodySmall, color = if (result.confidenceScore >= 80) Success else if (result.confidenceScore >= 50) Warning else Color.Red)
                }

                if (result.connectionContext != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(result.connectionContext.displayString, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
fun ResultItem(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
        if (unit.isNotEmpty()) Text(unit, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
    }
}

@Composable
fun TestMethodologySummary(result: SpeedTestResult) {
    Column {
        result.testProvenance?.let { prov ->
            Text("• Servers: ${prov.serversUsed.joinToString()}", style = MaterialTheme.typography.bodySmall)
            Text("• Threads: ${prov.threadsPerServer} per server", style = MaterialTheme.typography.bodySmall)
            Text("• Samples: ${prov.totalSamplesCollected} collected, ${prov.samplesDiscarded} discarded", style = MaterialTheme.typography.bodySmall)
            Text("• Duration: ${prov.testDurationSeconds}s", style = MaterialTheme.typography.bodySmall)
            Text("• Test ID: #${prov.testId}", style = MaterialTheme.typography.bodySmall)
        }
        result.aggregateData?.let { agg ->
            Text("• Cleaned: ${agg.displaySummary}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ConfidenceBreakdownContent(result: SpeedTestResult) {
    result.confidenceResult?.let { cr ->
        Text("Score: ${cr.score}/100", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("• Variance penalty: -${cr.breakdown.variancePenalty}", style = MaterialTheme.typography.bodySmall)
        Text("• Thread consistency penalty: -${cr.breakdown.threadPenalty}", style = MaterialTheme.typography.bodySmall)
        Text("• Packet loss penalty: -${cr.breakdown.packetLossPenalty}", style = MaterialTheme.typography.bodySmall)
        Text("• Bufferbloat penalty: -${cr.breakdown.bufferbloatPenalty}", style = MaterialTheme.typography.bodySmall)
    } ?: Text("Confidence calculation not available")
}

@Composable
fun BufferbloatCard(result: SpeedTestResult) {
    val gradeColor = when {
        result.bufferbloatGrade == "A_PLUS" || result.bufferbloatGrade == "A" -> Success
        result.bufferbloatGrade == "B" || result.bufferbloatGrade == "C" -> Warning
        else -> Color.Red
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Bufferbloat Grade", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(gradeColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(result.bufferbloatGrade.take(2), fontWeight = FontWeight.Bold, color = gradeColor)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Idle: ${String.format("%.0f", result.pingMs)}ms", style = MaterialTheme.typography.bodySmall)
            Text("Under download load: ${String.format("%.0f", result.loadedDownloadPingMs)}ms", style = MaterialTheme.typography.bodySmall)
            Text("Under upload load: ${String.format("%.0f", result.loadedUploadPingMs)}ms", style = MaterialTheme.typography.bodySmall)
            result.bufferbloatResult?.let { br ->
                Spacer(Modifier.height(8.dp))
                Text("Download increase: ${br.downloadIncreasePercent}% | Upload increase: ${br.uploadIncreasePercent}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun HealthScoreCard(health: com.rudra.internetspeedtest.report.HealthScore) {
    val color = when {
        health.score >= 75 -> Success
        health.score >= 55 -> Warning
        else -> Color.Red
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Connection Health", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("${health.score}/100", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
            }
            Spacer(Modifier.height(8.dp))
            Text("Grade: ${health.grade}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = color)
            Spacer(Modifier.height(4.dp))
            Text("Speed: ${health.speedAdequacy}/25 | Stability: ${health.stabilityScore}/25 | Bufferbloat: ${health.bufferbloatScore}/20", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Neutrality: ${health.neutralityScore}/15 | Real-Use: ${health.realUseScore}/15", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AnomaliesCard(anomalies: List<com.rudra.internetspeedtest.report.Anomaly>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Warning.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, null, Modifier.size(20.dp), tint = Warning)
                Spacer(Modifier.width(8.dp))
                Text("${anomalies.size} Anomal${if (anomalies.size == 1) "y" else "ies"} Detected", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            anomalies.forEach { anomaly ->
                Row(Modifier.padding(vertical = 4.dp)) {
                    Text("• ", color = when (anomaly.severity) { com.rudra.internetspeedtest.report.AnomalySeverity.CRITICAL -> Color.Red; com.rudra.internetspeedtest.report.AnomalySeverity.WARNING -> Warning; else -> Primary })
                    Text(anomaly.description, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun NeutralityResultCard(uiState: SpeedTestUiState) {
    val report = uiState.neutralityReport ?: return
    val score = report.neutralityScore
    val scoreColor = when {
        score >= 90 -> Success; score >= 70 -> Warning; else -> Color.Red
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Network Consistency", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Box(Modifier.size(48.dp).clip(CircleShape).background(scoreColor.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                    Text("$score", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = scoreColor)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(report.summary, style = MaterialTheme.typography.bodyMedium)
            if (report.variationDetected) {
                Spacer(Modifier.height(12.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Warning.copy(alpha = 0.1f)), shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        report.serviceResults.forEach { service ->
                            if (service.status != "normal") {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(service.serviceName, style = MaterialTheme.typography.bodyMedium)
                                    Text("${String.format("%.0f", service.deviationPercent)}% variation", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (service.deviationPercent > 0) Success else Warning)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(report.recommendation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun RealUseResultsSection(uiState: SpeedTestUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Real-World Performance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            uiState.streamingResult?.let { sr ->
                RealUseRow("YouTube", Icons.Default.Star, when (sr.recommendation) { com.rudra.internetspeedtest.feature.realuse.StreamingVerdict.EXCELLENT -> "${sr.stableResolution.label} stable"; else -> sr.stableResolution.label })
                RealUseRow("Buffer Health", null, "${sr.bufferHealthPercent}%")
                RealUseRow("Stalls", null, "${sr.stallEvents} events")
            }

            uiState.netflixResult?.let { nr ->
                Spacer(Modifier.height(8.dp))
                RealUseRow("Netflix", Icons.Default.Star, nr.verdict)
            }

            uiState.callQualityResult?.let { cq ->
                Spacer(Modifier.height(8.dp))
                RealUseRow("Video Calls", Icons.Default.Star, "${cq.resolution.label} | MOS: ${String.format("%.1f", cq.mosScore)}/5")
                RealUseRow("Jitter", null, "${String.format("%.1f", cq.jitterMs)}ms")
                RealUseRow("Packet Loss", null, "${String.format("%.2f", cq.packetLossPercent)}%")
            }

            uiState.gamingResult?.let { gr ->
                Spacer(Modifier.height(8.dp))
                RealUseRow("Gaming", Icons.Default.Star, gr.playabilityVerdict.label)
                RealUseRow("Ping", null, "${String.format("%.0f", gr.averagePingMs)}ms")
                RealUseRow("Spikes", null, "${gr.pingSpikes} detected")
            }

            uiState.socialMediaResult?.let { sm ->
                Spacer(Modifier.height(8.dp))
                RealUseRow("Social Media", null, sm.verdict)
            }

            uiState.downloadPredictions?.let { predictions ->
                Spacer(Modifier.height(8.dp))
                Text("Download Estimates", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                predictions.forEach { pred ->
                    RealUseRow(pred.fileSize.label, null, pred.estimatedTimeFormatted)
                }
            }

            uiState.householdCapacity?.let { hc ->
                Spacer(Modifier.height(8.dp))
                Text("Household Capacity", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("${hc.simultaneous4KStreams}x 4K + ${hc.simultaneousVideoCalls}x calls + ${hc.simultaneousGamingSessions}x gaming", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun RealUseRow(label: String, icon: ImageVector?, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, null, Modifier.size(16.dp), tint = Primary)
            Spacer(Modifier.width(4.dp))
        }
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ExpandableSection(title: String, content: @Composable () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ExpandMore, null, Modifier.size(20.dp))
            }
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                content()
            }
        }
    }
}

@Composable
fun SpeedTestInfoCard(onClickMethodology: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClickMethodology() },
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, modifier = Modifier.size(20.dp), tint = Primary)
                Spacer(Modifier.width(8.dp))
                Text("How we ensure accuracy", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            InfoRow("1.", "Multiple server tests (not just one)")
            InfoRow("2.", "Multi-threaded download for accurate speed")
            InfoRow("3.", "Statistical analysis removes outliers")
            InfoRow("4.", "Checks for ISP speed manipulation")
        }
    }
}

@Composable
fun InfoRow(number: String, text: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
        Text(number, style = MaterialTheme.typography.bodySmall, color = Primary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun StartSpeedTestButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(60.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Primary),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(Icons.Default.Speed, null, Modifier.size(28.dp))
        Spacer(Modifier.width(8.dp))
        Text("Start Speed Test", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, null, tint = Color.Red)
            Spacer(Modifier.width(8.dp))
            Text(message, color = Color.Red)
        }
    }
}

@Composable
fun MissingResultCard(reason: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(8.dp))
            Text(reason, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
