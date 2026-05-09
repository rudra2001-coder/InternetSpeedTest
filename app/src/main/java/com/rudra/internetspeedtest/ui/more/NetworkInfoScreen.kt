package com.rudra.internetspeedtest.ui.more

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rudra.internetspeedtest.core.network.ConnectionContext
import com.rudra.internetspeedtest.core.network.ConnectionDetector
import com.rudra.internetspeedtest.core.network.IpType
import com.rudra.internetspeedtest.core.network.NetworkType
import com.rudra.internetspeedtest.theme.Primary
import com.rudra.internetspeedtest.theme.Success
import com.rudra.internetspeedtest.theme.Surface
import com.rudra.internetspeedtest.theme.SurfaceVariant
import com.rudra.internetspeedtest.theme.Warning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkInfoScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val connectionDetector = remember { ConnectionDetector(context.applicationContext) }
    var connectionContext by remember { mutableStateOf<ConnectionContext?>(null) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        connectionContext = connectionDetector.detect()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Network Info") },
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
            connectionContext?.let { ctx ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Current Connection", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))

                        InfoRow("Network Type", ctx.networkType.name, when (ctx.networkType) {
                            NetworkType.WIFI -> Success
                            NetworkType.ETHERNET -> Success
                            NetworkType.CELLULAR -> Warning
                            else -> MaterialTheme.colorScheme.onSurface
                        })
                        InfoRow("ISP", ctx.ispName.ifEmpty { "Unknown" })
                        InfoRow("Carrier", ctx.carrierName.ifEmpty { "N/A" })
                        InfoRow("IP Type", ctx.ipType.name, when (ctx.ipType) {
                            IpType.PUBLIC -> Success
                            IpType.CGNAT -> Warning
                            IpType.PRIVATE -> Primary
                        })
                        ctx.signalStrength?.let { InfoRow("Signal", "${it}%") }
                        ctx.wifiFrequency?.let { InfoRow("WiFi Band", "${it.ghz}GHz") }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Connection Context", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(ctx.displayString, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } ?: Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("Unable to detect network info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("How We Detect", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Network type is detected using Android's ConnectivityManager. ISP name is extracted from DNS reverse lookup of your IP address. IP type classification follows RFC 1918 (private), RFC 6598 (CGNAT), and public ranges.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}
