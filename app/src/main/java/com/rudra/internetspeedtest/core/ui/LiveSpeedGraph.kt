package com.rudra.internetspeedtest.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.rudra.internetspeedtest.theme.Primary
import com.rudra.internetspeedtest.theme.Surface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SpeedPoint(
    val timestamp: Long,
    val speedMbps: Double
)

@Composable
fun LiveSpeedGraph(
    threadData: Map<Int, List<SpeedPoint>>,
    isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    val threadColors = listOf(Color.Blue, Color.Green, Color.Red, Color.Yellow)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Live Speed Graph", style = MaterialTheme.typography.titleSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, modifier = Modifier.weight(1f))
                if (isRunning) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(Color.Green))
                }
            }

            Spacer(Modifier.height(4.dp))

            Row {
                threadColors.forEachIndexed { i, color ->
                    if (threadData.containsKey(i)) {
                        Row(Modifier.padding(end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).clip(CircleShape).background(color))
                            Spacer(Modifier.size(4.dp))
                            Text("T${i + 1}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            val allPoints = threadData.values.flatten()
            if (allPoints.size >= 2) {
                val maxSpeed = allPoints.maxOf { it.speedMbps }.coerceAtLeast(1.0)
                val minTime = allPoints.minOf { it.timestamp }
                val maxTime = allPoints.maxOf { it.timestamp }
                val timeRange = (maxTime - minTime).coerceAtLeast(1L)

                Box(Modifier.fillMaxWidth().height(150.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height

                        threadData.forEach { (threadId, points) ->
                            val color = threadColors[threadId % threadColors.size]
                            val path = Path()

                            points.forEachIndexed { index, point ->
                                val x = if (timeRange > 0) {
                                    ((point.timestamp - minTime).toFloat() / timeRange) * width
                                } else 0f
                                val y = height - ((point.speedMbps / maxSpeed).toFloat() * height).coerceIn(0f, height)

                                if (index == 0) path.moveTo(x, y)
                                else path.lineTo(x, y)
                            }

                            drawPath(path, color, style = Stroke(width = 2.dp.toPx()))
                        }
                    }
                }
            } else {
                Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    Text("Collecting data...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
