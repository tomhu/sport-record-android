package com.example.sportrecord.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sportrecord.data.entity.SportRecord
import com.example.sportrecord.ui.components.BarChart
import com.example.sportrecord.ui.components.LineChart
import com.example.sportrecord.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(records: List<SportRecord>) {
    var period by remember { mutableStateOf("week") }
    val periods = listOf("近7天" to "week", "近30天" to "month", "全部" to "all")

    val today = LocalDate.now()
    val startDate = when (period) {
        "week" -> today.minusDays(7)
        "month" -> today.minusDays(30)
        else -> LocalDate.of(1970, 1, 1)
    }
    val filtered = records.filter { it.date >= startDate.toString() && it.date <= today.toString() }

    val totalCal = filtered.sumOf { it.calories }
    val totalDur = filtered.sumOf { it.duration }
    val totalCnt = filtered.size

    // 折线数据
    val lineData = remember(filtered) {
        val map = mutableMapOf<String, Int>()
        filtered.forEach { map[it.date] = (map[it.date] ?: 0) + it.calories }
        var cur = startDate
        val result = mutableListOf<Pair<String, Float>>()
        while (!cur.isAfter(today)) {
            val ds = cur.toString()
            result.add(ds.takeLast(5) to (map[ds] ?: 0).toFloat())
            cur = cur.plusDays(1)
        }
        result
    }

    // 柱状数据
    val barData = remember(filtered) {
        filtered.groupBy { it.sportName }.map { (k, v) -> k to v.sumOf { it.calories }.toFloat() }
            .sortedByDescending { it.second }.take(8)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Background),
        contentPadding = PaddingValues(24.dp)
    ) {
        item { Text("📊 运动统计", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold) }
        item { Spacer(Modifier.height(16.dp)) }

        // 周期切换
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                periods.forEach { (label, key) ->
                    FilterChip(
                        selected = period == key,
                        onClick = { period = key },
                        label = { Text(label, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryLight)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        if (filtered.isEmpty()) {
            item {
                Column(Modifier.fillMaxWidth().padding(60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📉", fontSize = 48.sp)
                    Text("暂无数据", fontWeight = FontWeight.Bold, color = TextMuted)
                }
            }
        } else {
            // 汇总
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryCard("🔥", "$totalCal", "消耗千卡", Primary, Modifier.weight(1f))
                    SummaryCard("🕐", "$totalDur", "运动分钟", Blue, Modifier.weight(1f))
                    SummaryCard("🏋", "$totalCnt", "运动次数", Amber, Modifier.weight(1f))
                }
            }

            // 折线图
            if (lineData.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(20.dp))
                    Surface(shape = RoundedCornerShape(20.dp), shadowElevation = 2.dp) {
                        Column(Modifier.padding(20.dp)) {
                            Text("📈 消耗趋势", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("每日消耗热量变化", fontSize = 12.sp, color = TextMuted)
                            Spacer(Modifier.height(8.dp))
                            LineChart(lineData, lineColor = Primary, yLabel = "千卡")
                        }
                    }
                }
            }

            // 柱状图
            if (barData.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(20.dp))
                    Surface(shape = RoundedCornerShape(20.dp), shadowElevation = 2.dp) {
                        Column(Modifier.padding(20.dp)) {
                            Text("🏆 运动排行", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(Modifier.height(8.dp))
                            BarChart(barData)
                        }
                    }
                }
            }

            // 月度汇总
            item {
                Spacer(Modifier.height(20.dp))
                Surface(shape = RoundedCornerShape(20.dp), shadowElevation = 2.dp) {
                    Column(Modifier.padding(20.dp)) {
                        Text("📅 月度汇总", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        records.groupBy { it.date.take(7) }.toList().sortedByDescending { it.first }.forEach { (month, rs) ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                                Text(month, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Spacer(Modifier.weight(1f))
                                Text("${rs.size}次 · ${rs.sumOf { it.calories }}千卡 · ${rs.sumOf { it.duration }}分钟",
                                    fontSize = 13.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
private fun SummaryCard(emoji: String, value: String, label: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(16.dp), shadowElevation = 1.dp) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 24.sp)
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, fontSize = 12.sp, color = TextMuted)
        }
    }
}
