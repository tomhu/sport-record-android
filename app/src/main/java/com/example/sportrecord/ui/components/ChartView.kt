package com.example.sportrecord.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sportrecord.ui.theme.*
import kotlin.math.max

@Composable
fun LineChart(
    data: List<Pair<String, Float>>,
    lineColor: Color = Primary,
    yLabel: String = "",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (yLabel.isNotEmpty())
            Text(yLabel, fontSize = 10.sp, color = TextMuted)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Surface, RoundedCornerShape(12.dp))
        ) {
            if (data.isEmpty()) return@Canvas
            val padLeft = 48f; val padRight = 16f; val padTop = 20f; val padBottom = 30f
            val plotW = size.width - padLeft - padRight
            val plotH = size.height - padTop - padBottom
            val maxVal = max(data.maxOf { it.second } * 1.1f, 1f)
            val gridLines = 4

            // Grid
            for (i in 0..gridLines) {
                val y = padTop + (plotH / gridLines) * i
                drawLine(Color(0x0F000000), Offset(padLeft, y), Offset(size.width - padRight, y), 1f)
            }

            // Data points
            val pts = mutableListOf<Offset>()
            data.forEachIndexed { idx, (_, v) ->
                val x = padLeft + (plotW / max(1, data.size - 1)) * idx
                val y = padTop + plotH - (v / maxVal) * plotH
                pts.add(Offset(x, y.coerceIn(padTop, padTop + plotH)))
            }

            // Fill
            if (pts.size > 1) {
                val path = Path().apply {
                    moveTo(pts.first().x, padTop + plotH)
                    pts.forEach { lineTo(it.x, it.y) }
                    lineTo(pts.last().x, padTop + plotH); close()
                }
                drawPath(path, Brush.verticalGradient(
                    listOf(lineColor.copy(alpha = 0.2f), lineColor.copy(alpha = 0.0f)),
                    startY = padTop, endY = padTop + plotH
                ))
            }

            // Line
            if (pts.size > 1) {
                val linePath = Path().apply {
                    moveTo(pts.first().x, pts.first().y)
                    pts.forEach { lineTo(it.x, it.y) }
                }
                drawPath(linePath, lineColor, style = Stroke(width = 3f, cap = StrokeCap.Round))
            }

            // Dots (sample every N)
            val dotStep = max(1, pts.size / 20)
            pts.forEachIndexed { idx, pt ->
                if (idx % dotStep == 0) {
                    drawCircle(Surface, 6f, pt)
                    drawCircle(lineColor, 4f, pt)
                }
            }
        }
    }
}

@Composable
fun BarChart(
    data: List<Pair<String, Float>>,
    colors: List<Color> = ChartColors,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(Surface, RoundedCornerShape(12.dp))
    ) {
        if (data.isEmpty()) return@Canvas
        val padLeft = 44f; val padRight = 16f; val padTop = 20f; val padBottom = 44f
        val plotW = size.width - padLeft - padRight
        val plotH = size.height - padTop - padBottom
        val maxVal = max(data.maxOf { it.second } * 1.12f, 1f)
        val gridLines = 4

        for (i in 0..gridLines) {
            val y = padTop + (plotH / gridLines) * i
            drawLine(Color(0x0F000000), Offset(padLeft, y), Offset(size.width - padRight, y), 1f)
        }

        val barW = minOf(44f, plotW / data.size * 0.55f)
        val gap = (plotW - barW * data.size) / (data.size + 1)

        data.forEachIndexed { idx, (_, v) ->
            val bx = padLeft + gap + (barW + gap) * idx
            val bh = (v / maxVal) * plotH
            val by = padTop + plotH - bh
            val color = colors[idx % colors.size]
            if (bh > 2f) {
                drawRoundRect(color.copy(alpha = 0.85f), Offset(bx, by), Size(barW, bh.coerceAtLeast(2f)), cornerRadius = CornerRadius(6f))
            }
        }
    }
}
