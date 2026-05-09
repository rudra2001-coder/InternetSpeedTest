package com.rudra.internetspeedtest.feature.realuse

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rudra.internetspeedtest.theme.Surface
import com.rudra.internetspeedtest.theme.Warning

data class RealUseHistoryEntry(
    val timestamp: Long,
    val youtubeQuality: String,
    val callQuality: String,
    val gamingQuality: String,
    val timeOfDay: String
)

@Composable
fun RealUseHistoryDashboard(
    history: List<RealUseHistoryEntry>,
    modifier: Modifier = Modifier
) {
    if (history.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Run real-use tests to see history", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(16.dp))
        }
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Real-Use History", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            val bestEntry = history.maxByOrNull { it.youtubeQuality.length }
            val worstEntry = history.minByOrNull { it.youtubeQuality.length }

            bestEntry?.let {
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("Best quality: ${it.youtubeQuality}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            worstEntry?.let {
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("Worst quality: ${it.youtubeQuality} (${it.timeOfDay})", style = MaterialTheme.typography.bodySmall, color = Warning)
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Recent tests:", style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            history.takeLast(5).reversed().forEach { entry ->
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    Text(entry.timeOfDay, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    Text("YouTube: ${entry.youtubeQuality}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
