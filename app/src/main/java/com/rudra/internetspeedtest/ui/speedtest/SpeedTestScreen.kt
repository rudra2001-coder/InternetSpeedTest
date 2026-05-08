package com.rudra.internetspeedtest.ui.speedtest

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.internetspeedtest.domain.model.NeutralityReport
import com.rudra.internetspeedtest.domain.model.TestPhase
import com.rudra.internetspeedtest.domain.model.TestStatus
import com.rudra.internetspeedtest.theme.Primary
import com.rudra.internetspeedtest.theme.Success
import com.rudra.internetspeedtest.theme.Surface
import com.rudra.internetspeedtest.theme.SurfaceVariant
import com.rudra.internetspeedtest.theme.Warning

@Composable
fun SpeedTestScreen(
    viewModel: SpeedTestViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.result == null) {
            SpeedTestHeroSection(
                isTestRunning = uiState.isTestRunning,
                phase = uiState.progress.phase
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isTestRunning) {
                TestProcessCard(progress = uiState.progress)
                Spacer(modifier = Modifier.height(8.dp))
                SpeedGraphCard(speedSamples = uiState.speedHistory)
            } else {
                SpeedTestInfoCard()
            }

            Spacer(modifier = Modifier.weight(1f))

            if (!uiState.isTestRunning) {
                StartSpeedTestButton(onClick = { viewModel.startTest() })
            }
        } else if (uiState.showNeutralityCheck) {
            SpeedTestResultsCard(result = uiState.result!!) {
                viewModel.runNeutralityCheck()
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (!uiState.isNeutralityRunning) {
                NeutralityCheckButton(onClick = { viewModel.runNeutralityCheck() })
            }
        } else if (uiState.neutralityReport != null) {
            NeutralityReportCard(report = uiState.neutralityReport!!) {
                viewModel.resetTest()
            }
        } else {
            SpeedTestResultsCard(result = uiState.result!!) {
                viewModel.resetTest()
            }
        }
    }
}

@Composable
fun SpeedTestHeroSection(isTestRunning: Boolean, phase: TestPhase) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Primary, Primary.copy(alpha = 0.7f))
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isTestRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = Color.White,
                            strokeWidth = 4.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Speed Test",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = when {
                        isTestRunning -> "Testing ${phase.name.lowercase()}..."
                        else -> "Test your real internet speed"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun TestProcessCard(progress: com.rudra.internetspeedtest.domain.model.SpeedTestProgress) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Testing Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            TestPhaseItem(
                phase = TestPhase.PING,
                currentPhase = progress.phase,
                title = "Ping Test",
                description = "Measuring latency to multiple servers",
                value = if (progress.pingMs > 0) "${String.format("%.1f", progress.pingMs)} ms" else "Testing...",
                isCompleted = progress.phase.ordinal > TestPhase.PING.ordinal || progress.phase == TestPhase.COMPLETE
            )

            TestPhaseItem(
                phase = TestPhase.DOWNLOAD,
                currentPhase = progress.phase,
                title = "Download Speed",
                description = "Testing with multiple connections",
                value = if (progress.phase.ordinal >= TestPhase.DOWNLOAD.ordinal && progress.currentSpeed > 0)
                    "${String.format("%.1f", progress.currentSpeed)} Mbps" else "",
                isCompleted = progress.phase.ordinal > TestPhase.DOWNLOAD.ordinal || progress.phase == TestPhase.COMPLETE
            )

            TestPhaseItem(
                phase = TestPhase.UPLOAD,
                currentPhase = progress.phase,
                title = "Upload Speed",
                description = "Testing upload bandwidth",
                value = if (progress.phase.ordinal >= TestPhase.UPLOAD.ordinal && progress.currentSpeed > 0)
                    "${String.format("%.1f", progress.currentSpeed)} Mbps" else "",
                isCompleted = progress.phase.ordinal > TestPhase.UPLOAD.ordinal || progress.phase == TestPhase.COMPLETE
            )

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { progress.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Primary,
                trackColor = Primary.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = progress.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TestPhaseItem(
    phase: TestPhase,
    currentPhase: TestPhase,
    title: String,
    description: String,
    value: String,
    isCompleted: Boolean
) {
    val isActive = currentPhase == phase
    val color = when {
        isCompleted -> Success
        isActive -> Primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isCompleted || isActive) color.copy(alpha = 0.2f)
                    else SurfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Success,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                val icon = when (phase) {
                    TestPhase.PING -> Icons.Default.Wifi
                    TestPhase.DOWNLOAD -> Icons.Default.Download
                    TestPhase.UPLOAD -> Icons.Default.CloudUpload
                    else -> Icons.Default.Info
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = color
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        if (value.isNotEmpty()) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun SpeedTestInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "How we ensure accuracy",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            InfoRow("1.", "Multiple server tests (not just one)")
            InfoRow("2.", "Multi-threaded download for accurate speed")
            InfoRow("3.", "Statistical analysis removes outliers")
            InfoRow("4.", "Checks for ISP speed manipulation")
        }
    }
}

@Composable
fun InfoRow(number: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = number,
            style = MaterialTheme.typography.bodySmall,
            color = Primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StartSpeedTestButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Primary),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Speed,
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Start Speed Test",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SpeedTestResultsCard(result: com.rudra.internetspeedtest.domain.model.SpeedTestResult, onReset: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Primary, Primary.copy(alpha = 0.8f))
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Test Results",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ResultItem(
                        label = "Download",
                        value = String.format("%.1f", result.downloadSpeedMbps),
                        unit = "Mbps"
                    )
                    ResultItem(
                        label = "Upload",
                        value = String.format("%.1f", result.uploadSpeedMbps),
                        unit = "Mbps"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ResultItem(
                        label = "Ping",
                        value = String.format("%.0f", result.pingMs),
                        unit = "ms"
                    )
                    if (result.loadedDownloadPingMs > 0) {
                        ResultItem(
                            label = "Loaded Ping",
                            value = String.format("%.0f", result.loadedDownloadPingMs),
                            unit = "ms"
                        )
                    }
                    ResultItem(
                        label = "Bufferbloat",
                        value = result.bufferbloatGrade,
                        unit = ""
                    )
                }

                if (result.connectionType.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Connection: ${result.connectionType} | ISP: ${result.ispName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                if (result.confidenceScore > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Confidence: ${result.confidenceScore}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (result.confidenceScore >= 80) Success else if (result.confidenceScore >= 50) Warning else Color.Red
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onReset,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test Again", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ResultItem(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        if (unit.isNotEmpty()) {
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
fun NeutralityCheckButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Warning.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = Warning
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Check Network Consistency", color = Warning)
    }
}

@Composable
fun NeutralityReportCard(
    report: NeutralityReport,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Network Consistency Report",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                report.neutralityScore >= 90 -> Success.copy(alpha = 0.2f)
                                report.neutralityScore >= 70 -> Warning.copy(alpha = 0.2f)
                                else -> Color.Red.copy(alpha = 0.2f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${report.neutralityScore}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            report.neutralityScore >= 90 -> Success
                            report.neutralityScore >= 70 -> Warning
                            else -> Color.Red
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = report.summary,
                style = MaterialTheme.typography.bodyMedium
            )

            if (report.variationDetected) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Warning.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        report.serviceResults.forEach { service ->
                            if (service.status != "normal") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = service.serviceName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "${String.format("%.0f", service.deviationPercent)}% variation",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (service.deviationPercent > 0) Success else Warning
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = report.recommendation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onReset,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Replay,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Test", color = Color.White)
            }
        }
    }
}
