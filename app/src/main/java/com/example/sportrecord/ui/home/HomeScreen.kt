package com.example.sportrecord.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sportrecord.data.entity.*
import com.example.sportrecord.ui.components.RecordCard
import com.example.sportrecord.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@Composable
fun HomeScreen(
    currentUser: User?,
    viewLabel: String,
    records: List<SportRecord>,
    onAdd: () -> Unit,
    onDelete: (String) -> Unit,
    onTapRecord: (SportRecord) -> Unit
) {
    val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    val todayRecords = records.filter { it.date == today }
    val historyRecords = records.filter { it.date != today }

    // 今日统计
    val todayStats = remember(todayRecords) {
        Triple(
            todayRecords.size,
            todayRecords.sumOf { it.duration },
            todayRecords.sumOf { it.calories }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(Background),
            contentPadding = PaddingValues(24.dp, 16.dp, 24.dp, 100.dp)
        ) {
            // 用户信息栏
            if (currentUser != null) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(44.dp).clip(CircleShape)
                            .background(Brush.linearGradient(listOf(PrimaryLight, Color(0xFFA7F3D0)))),
                            contentAlignment = Alignment.Center
                        ) { Text(currentUser.avatar, fontSize = 20.sp) }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(currentUser.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            if (currentUser.isAdmin) Text("管理员", fontSize = 11.sp, color = Amber)
                        }
                        Spacer(Modifier.weight(1f))
                        if (viewLabel.isNotEmpty()) {
                            Surface(shape = RoundedCornerShape(12.dp), color = PrimaryLight) {
                                Text("👁 $viewLabel", fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                            }
                        }
                    }
                }
            }

            if (records.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 120.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🏃‍♂️", fontSize = 64.sp)
                        Text("还没有运动记录", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Text("点击下方按钮开始", fontSize = 13.sp, color = TextMuted)
                    }
                }
            }

            // 今日统计卡片
            if (records.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .background(Brush.horizontalGradient(listOf(GradientStart, GradientEnd)), RoundedCornerShape(24.dp))
                            .padding(24.dp)
                    ) {
                        Column {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("今日运动", color = Surface, fontWeight = FontWeight.SemiBold)
                                Text(LocalDate.now().format(DateTimeFormatter.ofPattern("M月d日")) +
                                    " 周${LocalDate.now().dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINA)}",
                                    color = Surface.copy(alpha = 0.8f), fontSize = 13.sp)
                            }
                            Spacer(Modifier.height(16.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                StatItem("🏋️", "${todayStats.first}", "运动次数")
                                StatItem("🕐", "${todayStats.second}", "运动分钟")
                                StatItem("🔥", "${todayStats.third}", "消耗千卡", highlight = true)
                            }
                        }
                    }
                }
            }

            // 今日记录
            if (todayRecords.isNotEmpty()) {
                item { SectionHeader("今天", "${todayRecords.size} 项") }
                items(todayRecords, key = { it.id }) { r ->
                    RecordCard(r, onDelete = { onDelete(r.id) }, onTap = { onTapRecord(r) })
                    Spacer(Modifier.height(12.dp))
                }
            }

            // 历史记录按日期分组
            if (historyRecords.isNotEmpty()) {
                item { SectionHeader("历史记录") }
                val grouped = historyRecords.groupBy { it.date }.toList().sortedByDescending { it.first }
                grouped.forEach { (date, rs) ->
                    val dayOfWeek = try {
                        LocalDate.parse(date).dayOfWeek.getDisplayName(TextStyle.FULL, Locale.CHINA)
                    } catch (_: Exception) { "" }
                    val sumCal = rs.sumOf { it.calories }
                    item {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 12.dp)) {
                            Text("$date $dayOfWeek", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextSecondary)
                            Spacer(Modifier.weight(1f))
                            Text("${rs.size}次 · ${sumCal}千卡", fontSize = 12.sp, color = Primary)
                        }
                    }
                    items(rs, key = { it.id }) { r ->
                        RecordCard(r, onDelete = { onDelete(r.id) }, onTap = { onTapRecord(r) })
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp).size(64.dp),
            shape = CircleShape,
            containerColor = Primary
        ) { Text("+", fontSize = 28.sp, color = Surface, fontWeight = FontWeight.Light) }
    }
}

@Composable
private fun StatItem(emoji: String, value: String, label: String, highlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 20.sp)
        Text(value, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold,
            color = if (highlight) Color(0xFFFDE68A) else Surface)
        Text(label, fontSize = 12.sp, color = Surface.copy(alpha = 0.75f))
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String? = null) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        if (subtitle != null) {
            Spacer(Modifier.weight(1f))
            Text(subtitle, fontSize = 13.sp, color = TextMuted)
        }
    }
}
