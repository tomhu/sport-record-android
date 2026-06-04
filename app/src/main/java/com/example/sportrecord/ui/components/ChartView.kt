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
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sportrecord.ui.theme.*
import kotlin.math.*

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
                .padding(8.dp)
        ) {
            if (data.isEmpty()) return@Canvas
            val padLeft = 48f; val padRight = 16f; val padTop = 24f; val padBottom = 36f
            val plotW = size.width - padLeft - padRight
            val plotH = size.height - padTop - padBottom

            val maxVal = max(data.maxOf { it.second } * 1.1f, 1f)

            // 网格线
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = padTop + (plotH / gridLines) * i
                drawLine(Color(0x0F000000), Offset(padLeft, y), Offset(size.width - padRight, y), 1f)
            }

            // Y 轴标签
            val txtPaint = android.graphics.Paint().apply { color = 0x55000000; textSize = 22f; textAlign = android.graphics.Paint.Align.RIGHT }
            for (i in 0..gridLines) {
                val v = (maxVal - (maxVal / gridLines) * i).toInt()
                drawContext.canvas.nativeCanvas.drawText(
                    "$v", padLeft - 8f, padTop + (plotH / gridLines) * i + 6f, txtPaint
                )
            }

            // X 轴
            val stepX = max(1, (data.size - 1) / 4)
            val xTxtPaint = android.graphics.Paint().apply { color = 0x55000000; textSize = 22f; textAlign = android.graphics.Paint.Align.CENTER }
            data.forEachIndexed { idx, (label, _) ->
                if (idx == 0 || idx == data.lastIndex || idx % stepX == 0) {
                    val x = padLeft + (plotW / (data.size - 1)) * idx
                    drawContext.canvas.nativeCanvas.drawText(
                        label, x, size.height - 4f, xTxtPaint
                    )
                }
            }

            // 数据点
            var prevPt: Offset? = null
            val pts = mutableListOf<Offset>()
            data.forEachIndexed { idx, (_, v) ->
                val x = padLeft + (plotW / max(1, data.size - 1)) * idx
                val y = padTop + plotH - (v / maxVal) * plotH
                pts.add(Offset(x, y))
            }

            // 填充
            if (pts.size > 1) {
                val path = Path().apply {
                    moveTo(pts.first().x, padTop + plotH)
                    pts.forEach { lineTo(it.x, it.y) }
                    lineTo(pts.last().x, padTop + plotH); close()
                }
                drawPath(path, Brush.verticalGradient(listOf(
                    lineColor.copy(alpha = 0.25f), lineColor.copy(alpha = 0.0f)
                ), startY = padTop, endY = padTop + plotH))
            }

            // 折线
            if (pts.size > 1) {
                val linePath = Path().apply {
                    moveTo(pts.first().x, pts.first().y)
                    pts.forEach { lineTo(it.x, it.y) }
                }
                drawPath(linePath, lineColor, style = Stroke(width = 4f, cap = StrokeCap.Round))
            }

            // 圆点
            pts.forEach { pt ->
                drawCircle(Surface, 8f, pt)
                drawCircle(lineColor, 5f, pt)
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
            .padding(8.dp)
    ) {
        if (data.isEmpty()) return@Canvas
        val padLeft = 44f; val padRight = 16f; val padTop = 24f; val padBottom = 48f
        val plotW = size.width - padLeft - padRight
        val plotH = size.height - padTop - padBottom

        val maxVal = max(data.maxOf { it.second } * 1.12f, 1f)

        // 网格
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = padTop + (plotH / gridLines) * i
            drawLine(Color(0x0F000000), Offset(padLeft, y), Offset(size.width - padRight, y), 1f)
        }

        val txtPaint = android.graphics.Paint().apply { color = 0x55000000; textSize = 22f; textAlign = android.graphics.Paint.Align.RIGHT }
        for (i in 0..gridLines) {
            val v = (maxVal - (maxVal / gridLines) * i).toInt()
            drawContext.canvas.nativeCanvas.drawText("$v", padLeft - 6f, padTop + (plotH / gridLines) * i + 6f, txtPaint)
        }

        val barW = min(48f, plotW / data.size * 0.6f)
        val gap = (plotW - barW * data.size) / (data.size + 1)

        data.forEachIndexed { idx, (label, v) ->
            val bx = padLeft + gap + (barW + gap) * idx
            val bh = (v / maxVal) * plotH
            val by = padTop + plotH - bh
            val color = colors[idx % colors.size]

            drawRoundRect(color.copy(alpha = 0.8f), Offset(bx, by), Size(barW, max(bh, 2f)), 6f)

            // 数值
            if (bh > 24) {
                val vTxtPaint = android.graphics.Paint().apply {
                    this.color = 0x88000000; textSize = 22f; textAlign = android.graphics.Paint.Align.CENTER; isFakeBoldText = true
                }
                drawContext.canvas.nativeCanvas.drawText("${v.toInt()}", bx + barW / 2, by - 4f, vTxtPaint)
            }

            // X 标签
            val lt = if (label.length > 4) label.take(4) + ".." else label
            val lTxtPaint = android.graphics.Paint().apply { color = 0x66000000; textSize = 20f; textAlign = android.graphics.Paint.Align.CENTER }
            drawContext.canvas.nativeCanvas.drawText(lt, bx + barW / 2, size.height - 4f, lTxtPaint)
        }
    }
}
