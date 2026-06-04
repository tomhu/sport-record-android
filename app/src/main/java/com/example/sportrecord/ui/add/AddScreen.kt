package com.example.sportrecord.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sportrecord.model.*
import com.example.sportrecord.ui.theme.*
import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    weight: Int,
    onSave: (sport: SportDef, date: String, duration: Int, count: Int, distance: Double, calories: Int) -> Unit,
    onCycling: () -> Unit,
    onBack: () -> Unit
) {
    var selectedSport by remember { mutableStateOf<SportDef?>(null) }
    var date by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    var duration by remember { mutableStateOf("") }
    var count by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    val dur = duration.toIntOrNull() ?: 0; val cnt = count.toIntOrNull() ?: 0; val dist = distance.toDoubleOrNull() ?: 0.0

    val calories = remember(selectedSport, dur, cnt, dist) {
        val s = selectedSport ?: return@remember 0
        var cal = 0
        if ((s.measureType == "duration" || s.measureType == "both") && dur > 0)
            cal += CalorieEngine.byDuration(s.met, weight, dur)
        if ((s.measureType == "count" || s.measureType == "both") && cnt > 0)
            cal += CalorieEngine.byCount(cnt, s.kcalPerUnit)
        if (s.caloriePerKm > 0 && dist > 0 && dur == 0)
            cal += CalorieEngine.byDistance(dist, weight, s.caloriePerKm)
        cal
    }

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        TopAppBar(
            title = { Text("添加记录", fontWeight = FontWeight.Bold) },
            navigationIcon = { TextButton(onClick = onBack) { Text("取消", color = TextSecondary) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            // ── 表单（选中运动后置顶，选中时自动滚动到此处） ──
            if (selectedSport != null) {
                val s = selectedSport!!
                Surface(shape = RoundedCornerShape(20.dp), color = Surface, shadowElevation = 3.dp) {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(s.icon, fontSize = 24.sp)
                            Spacer(Modifier.width(12.dp))
                            Text(s.name.replace(Regex("[^一-龥]"), ""), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(Modifier.weight(1f))
                            TextButton(onClick = { selectedSport = null }) { Text("换项目", fontSize = 13.sp, color = TextMuted) }
                        }
                        Spacer(Modifier.height(12.dp))

                        Text("运动日期", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(date, {}, readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = { Text("📅", modifier = Modifier.clickable { showDatePicker = true }) })
                        Spacer(Modifier.height(12.dp))

                        if (s.measureType == "duration" || s.measureType == "both") {
                            Text("时长 (分钟)", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(duration, { duration = it }, placeholder = { Text("如 30") },
                                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                            Spacer(Modifier.height(12.dp))
                        }
                        if (s.caloriePerKm > 0) {
                            Text("距离 (公里)", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(distance, { distance = it }, placeholder = { Text("如 5.0") },
                                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                            Spacer(Modifier.height(12.dp))
                        }
                        if (s.measureType == "count" || s.measureType == "both") {
                            Text("次数", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(count, { count = it }, placeholder = { Text("如 500") },
                                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                            Spacer(Modifier.height(12.dp))
                        }
                        if (calories > 0) {
                            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = Color(0xFFFFF7ED)) {
                                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("🔥", fontSize = 28.sp)
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text("预估消耗", fontSize = 12.sp, color = Color(0xFF9A3412))
                                        Text("$calories 千卡", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFEA580C))
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { onSave(s, date, dur, cnt, dist, calories) },
                            enabled = (dur > 0 || cnt > 0 || dist > 0) && calories > 0,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) { Text(if (calories > 0) "✓ 保存记录" else "填写运动量以保存", fontWeight = FontWeight.Bold) }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // ── 运动项目网格 ──
            Text("选择运动项目", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            SportsData.allSports.chunked(3).forEach { row ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { sport ->
                        val sel = selectedSport?.key == sport.key
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (sport.key == "cycling") { onCycling(); return@clickable }
                                    selectedSport = sport
                                    scope.launch { scrollState.animateScrollTo(0) }
                                },
                            shape = RoundedCornerShape(16.dp),
                            color = if (sel) PrimaryLight else Color(0xFFF8FAFC),
                            border = if (sel) androidx.compose.foundation.BorderStroke(2.dp, Primary) else null
                        ) {
                            Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(sport.icon, fontSize = 24.sp)
                                Text(sport.name.replace(Regex("[^一-龥]"), ""),
                                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    if (sport.caloriePerKm > 0) "计时/距离"
                                    else when(sport.measureType) { "duration" -> "计时"; "count" -> "计次"; else -> "计时+计次" },
                                    fontSize = 10.sp, color = TextMuted)
                            }
                        }
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        date = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                            .format(DateTimeFormatter.ISO_LOCAL_DATE)
                    }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } }
        ) { DatePicker(state = datePickerState) }
    }
}
