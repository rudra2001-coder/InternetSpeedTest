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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.internetspeedtest.domain.model.NeutralityReport
import com.rudra.internetspeedtest.theme.Primary
import com.rudra.internetspeedtest.theme.Success
import com.rudra.internetspeedtest.theme.Surface
import com.rudra.internetspeedtest.theme.SurfaceVariant
import com.rudra.internetspeedtest.theme.Warning
import com.rudra.internetspeedtest.ui.speedtest.SpeedTestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkNeutralityScreen(
    onNavigateBack: () -> Unit,
    viewModel: SpeedTestViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Network Consistency") },
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
                    Text("Network Neutrality Check", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Tests whether your ISP treats all services equally by comparing speeds to multiple endpoints.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))

                    Text("Tested Services", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    listOf("YouTube", "Netflix", "Facebook", "Instagram", "WhatsApp", "Telegram", "GitHub", "Cloudflare").forEach { service ->
                        Row(Modifier.padding(vertical = 2.dp)) {
                            Box(Modifier.size(6.dp).background(Primary, CircleShape))
                            Spacer(Modifier.width(8.dp))
                            Text(service, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (uiState.neutralityReport == null && !uiState.isNeutralityRunning) {
                Button(
                    onClick = {
                        if (uiState.result != null) viewModel.runNeutralityCheck()
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Warning.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(16.dp),
                    enabled = uiState.result != null
                ) {
                    Icon(Icons.Default.Info, null, tint = Warning)
                    Spacer(Modifier.width(8.dp))
                    Text(if (uiState.result != null) "Run Neutrality Check" else "Run a speed test first", color = Warning)
                }
            }

            if (uiState.isNeutralityRunning) {
                Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(16.dp)) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Checking network consistency across services...")
                    }
                }
            }

            uiState.neutralityReport?.let { report ->
                Spacer(Modifier.height(16.dp))
                NeutralityDetailCard(report)
            }
        }
    }
}

@Composable
fun NeutralityDetailCard(report: NeutralityReport) {
    val scoreColor = when {
        report.neutralityScore >= 90 -> Success
        report.neutralityScore >= 70 -> Warning
        else -> Color.Red
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Results", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Box(Modifier.size(56.dp).background(scoreColor.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${report.neutralityScore}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = scoreColor)
                        Text("score", style = MaterialTheme.typography.labelSmall, color = scoreColor)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Baseline: ${String.format("%.1f", report.baselineSpeedMbps)} Mbps", style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(12.dp))

            report.serviceResults.forEach { service ->
                val color = when (service.status) {
                    "elevated" -> Success
                    "reduced" -> Warning
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Text(service.serviceName, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text("${String.format("%.0f", service.deviationPercent)}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = color)
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(colors = CardDefaults.cardColors(containerColor = SurfaceVariant), shape = RoundedCornerShape(12.dp)) {
                Text(report.summary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp))
            }

            if (report.recommendation.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(report.recommendation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
