package com.rudra.internetspeedtest.ui.more

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.internetspeedtest.feature.realuse.CallQualityResult
import com.rudra.internetspeedtest.feature.realuse.DownloadPrediction
import com.rudra.internetspeedtest.feature.realuse.GamingResult
import com.rudra.internetspeedtest.feature.realuse.HouseholdCapacity
import com.rudra.internetspeedtest.feature.realuse.SocialMediaResult
import com.rudra.internetspeedtest.feature.realuse.StreamingSimResult
import com.rudra.internetspeedtest.feature.realuse.StreamingVerdict
import com.rudra.internetspeedtest.theme.Primary
import com.rudra.internetspeedtest.theme.Success
import com.rudra.internetspeedtest.theme.Surface
import com.rudra.internetspeedtest.theme.SurfaceVariant
import com.rudra.internetspeedtest.theme.Warning
import com.rudra.internetspeedtest.ui.speedtest.SpeedTestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealUseTestsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SpeedTestViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Real-World Tests") },
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("Real-Use Simulations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Tests simulate real activities: YouTube streaming, Netflix, video calls, gaming, social media scrolling, and file downloads.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(16.dp))

            if (uiState.streamingResult == null && !uiState.isRealUseRunning) {
                Button(
                    onClick = { viewModel.runRealUseTests() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(16.dp),
                    enabled = uiState.result != null
                ) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (uiState.result != null) "Run All Real-Use Tests" else "Run a speed test first")
                }
            }

            if (uiState.isRealUseRunning) {
                Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(16.dp)) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Running simulations...")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            uiState.streamingResult?.let { result ->
                ResultSectionCard("YouTube Streaming") {
                    DetailRow("Resolution", "${result.stableResolution.label} stable")
                    DetailRow("Buffer Health", "${result.bufferHealthPercent}%")
                    DetailRow("Stall Events", "${result.stallEvents}")
                    DetailRow("Quality Switches", "${result.qualitySwitches}")
                    val verdictColor = when (result.recommendation) {
                        StreamingVerdict.EXCELLENT, StreamingVerdict.GOOD -> Success
                        StreamingVerdict.ADEQUATE -> Warning
                        else -> Color.Red
                    }
                    DetailRow("Verdict", result.recommendation.name, verdictColor)
                }
            }

            uiState.netflixResult?.let { nr ->
                ResultSectionCard("Netflix") {
                    DetailRow("Speed", "${String.format("%.1f", nr.speedMbps)} Mbps")
                    DetailRow("4K Ready", if (nr.q4kReady) "Yes" else "No")
                    DetailRow("HDR Ready", if (nr.hdrReady) "Yes" else "No")
                    DetailRow("Verdict", nr.verdict, if (nr.q4kReady) Success else Warning)
                }
            }

            uiState.callQualityResult?.let { cq ->
                val mosColor = when {
                    cq.mosScore >= 4.0 -> Success
                    cq.mosScore >= 3.0 -> Warning
                    else -> Color.Red
                }
                ResultSectionCard("Video Calls") {
                    DetailRow("Resolution", cq.resolution.label)
                    DetailRow("MOS Score", "${String.format("%.1f", cq.mosScore)}/5", mosColor)
                    DetailRow("Jitter", "${String.format("%.1f", cq.jitterMs)} ms")
                    DetailRow("Packet Loss", "${String.format("%.2f", cq.packetLossPercent)}%")
                    DetailRow("Verdict", cq.verdict.name)
                }
            }

            uiState.gamingResult?.let { gr ->
                val gamingColor = when {
                    gr.playabilityVerdict.ordinal <= 1 -> Success
                    gr.playabilityVerdict.ordinal == 2 -> Warning
                    else -> Color.Red
                }
                ResultSectionCard("Gaming") {
                    DetailRow("Avg Ping", "${String.format("%.0f", gr.averagePingMs)} ms")
                    DetailRow("Jitter", "${String.format("%.1f", gr.pingJitterMs)} ms")
                    DetailRow("Spikes", "${gr.pingSpikes}")
                    DetailRow("Packet Loss", "${String.format("%.1f", gr.packetLoss * 100)}%")
                    DetailRow("Verdict", gr.playabilityVerdict.label, gamingColor)
                }
            }

            uiState.socialMediaResult?.let { sm ->
                ResultSectionCard("Social Media") {
                    DetailRow("Image Load", "${String.format("%.0f", sm.imageLoadTimeMs)} ms")
                    DetailRow("Video Preload", "${String.format("%.0f", sm.videoPreloadTimeMs)} ms")
                    DetailRow("Smoothness", "${sm.scrollSmoothness}%")
                    Text(sm.verdict, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            uiState.downloadPredictions?.let { predictions ->
                ResultSectionCard("Download Estimates") {
                    predictions.forEach { pred ->
                        DetailRow(pred.fileSize.label, pred.estimatedTimeFormatted)
                        pred.stabilityWarning?.let { warning ->
                            Text(warning, style = MaterialTheme.typography.bodySmall, color = Warning)
                        }
                    }
                }
            }

            uiState.householdCapacity?.let { hc ->
                ResultSectionCard("Household Capacity") {
                    DetailRow("4K Streams", "${hc.simultaneous4KStreams}x")
                    DetailRow("Video Calls", "${hc.simultaneousVideoCalls}x")
                    DetailRow("Gaming Sessions", "${hc.simultaneousGamingSessions}x")
                    Text(hc.recommendation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun ResultSectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Primary)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}
