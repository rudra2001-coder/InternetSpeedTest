package com.rudra.internetspeedtest.ui.more

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rudra.internetspeedtest.theme.Primary
import com.rudra.internetspeedtest.theme.Success
import com.rudra.internetspeedtest.theme.Surface
import com.rudra.internetspeedtest.theme.SurfaceVariant
import com.rudra.internetspeedtest.theme.Warning

data class FeatureItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String,
    val color: Color
)

@Composable
fun MoreScreen(navController: NavController) {
    val features = listOf(
        FeatureItem(
            title = "Speed Test",
            description = "Test download & upload speed",
            icon = Icons.Default.Speed,
            route = "speedtest",
            color = Primary
        ),
        FeatureItem(
            title = "CDN Test",
            description = "Test CDN performance",
            icon = Icons.Default.CloudUpload,
            route = "dashboard",
            color = Success
        ),
        FeatureItem(
            title = "History",
            description = "View past test results",
            icon = Icons.Default.History,
            route = "history",
            color = Warning
        ),
        FeatureItem(
            title = "Settings",
            description = "App preferences & config",
            icon = Icons.Default.Settings,
            route = "settings",
            color = Primary.copy(alpha = 0.7f)
        ),
        FeatureItem(
            title = "Network Info",
            description = "Current network details",
            icon = Icons.Default.Wifi,
            route = "network-info",
            color = Success.copy(alpha = 0.8f)
        ),
        FeatureItem(
            title = "Connection Health",
            description = "Health score & anomalies",
            icon = Icons.Default.Favorite,
            route = "connection-health",
            color = Color(0xFFE91E63)
        ),
        FeatureItem(
            title = "Network Consistency",
            description = "Neutrality & throttling check",
            icon = Icons.Default.Extension,
            route = "network-neutrality",
            color = Color(0xFF9C27B0)
        ),
        FeatureItem(
            title = "Real-World Tests",
            description = "Simulate real usage",
            icon = Icons.Default.PlayArrow,
            route = "realuse-tests",
            color = Color(0xFF00BCD4)
        ),
        FeatureItem(
            title = "Export Data",
            description = "Export or delete test history",
            icon = Icons.Default.Download,
            route = "export-data",
            color = Color(0xFFFF9800)
        ),
        FeatureItem(
            title = "About",
            description = "App info & version",
            icon = Icons.Default.Info,
            route = "about",
            color = SurfaceVariant
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(Primary, Primary.copy(alpha = 0.7f))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "More Features",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Access all app functionalities",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(features) { feature ->
                FeatureGridItem(feature = feature) {
                    navController.navigate(feature.route) {
                        popUpTo("more") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureGridItem(
    feature: FeatureItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(feature.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = feature.title,
                    modifier = Modifier.size(28.dp),
                    tint = feature.color
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = feature.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = feature.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}
