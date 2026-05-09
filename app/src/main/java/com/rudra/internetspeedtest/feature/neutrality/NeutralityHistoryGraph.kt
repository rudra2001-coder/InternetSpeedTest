package com.rudra.internetspeedtest.feature.neutrality

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.rudra.internetspeedtest.theme.Primary
import com.rudra.internetspeedtest.theme.Success
import com.rudra.internetspeedtest.theme.Surface
import com.rudra.internetspeedtest.theme.Warning

@Composable
fun NeutralityHistoryGraph(
    history: List<NeutralityScore>,
    modifier: Modifier = Modifier
) {
    if (history.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Network Consistency Over Time", style = MaterialTheme.typography.titleSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            Box(Modifier.fillMaxWidth().height(150.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    val greenY = height * (1 - 90f / 100f)
                    val yellowY = height * (1 - 70f / 100f)
                    val redY = height * (1 - 50f / 100f)

                    drawLine(Success.copy(alpha = 0.3f), Offset(0f, greenY), Offset(width, greenY), strokeWidth = 1.dp.toPx())
                    drawLine(Warning.copy(alpha = 0.3f), Offset(0f, yellowY), Offset(width, yellowY), strokeWidth = 1.dp.toPx())
                    drawLine(Color.Red.copy(alpha = 0.3f), Offset(0f, redY), Offset(width, redY), strokeWidth = 1.dp.toPx())

                    if (history.size >= 2) {
                        val path = Path()
                        history.forEachIndexed { index, score ->
                            val x = (index.toFloat() / (history.size - 1)) * width
                            val y = height * (1 - score.score / 100f)
                            if (index == 0) path.moveTo(x, y)
                            else path.lineTo(x, y)
                        }
                        drawPath(path, Primary, style = Stroke(width = 2.dp.toPx()))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            val trend = if (history.size >= 3) {
                val recent = history.takeLast(3).map { it.score }.average()
                val older = history.take(history.size - 3).map { it.score }.average()
                when {
                    recent < older - 5 -> "Trending downward: consistency decreasing"
                    recent > older + 5 -> "Trending upward: consistency improving"
                    else -> "Consistency stable over time"
                }
            } else "Run more tests to see trends"

            Text(trend, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
