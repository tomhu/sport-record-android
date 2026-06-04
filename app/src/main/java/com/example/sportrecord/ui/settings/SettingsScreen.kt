package com.example.sportrecord.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sportrecord.data.entity.User
import com.example.sportrecord.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentUser: User?,
    weight: Int,
    viewMode: String,
    allUsers: List<User>,
    totalRecords: Int,
    onWeightSave: (Int) -> Unit,
    onSwitchUser: () -> Unit,
    onViewModeChange: (String) -> Unit,
    onClearRecords: () -> Unit
) {
    var weightInput by remember { mutableStateOf(weight.toString()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Background),
        contentPadding = PaddingValues(24.dp)
    ) {
        // 用户
        if (currentUser != null) {
            item {
                Surface(shape = RoundedCornerShape(20.dp), shadowElevation = 2.dp) {
                    Row(Modifier.fillMaxWidth().padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(56.dp).background(Brush.linearGradient(listOf(PrimaryLight, Color(0xFFA7F3D0))), RoundedCornerShape(28.dp)),
                            contentAlignment = Alignment.Center) { Text(currentUser.avatar, fontSize = 28.sp) }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(currentUser.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(if (currentUser.isAdmin) "管理员" else "普通用户", fontSize = 12.sp,
                                color = if (currentUser.isAdmin) Amber else TextMuted)
                        }
                        OutlinedButton(onClick = onSwitchUser) { Text("切换") }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // 管理员面板
            if (currentUser.isAdmin) {
                item {
                    Surface(shape = RoundedCornerShape(20.dp), shadowElevation = 2.dp) {
                        Column(Modifier.padding(24.dp)) {
                            Text("🛡 管理员面板", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("选择要查看的数据范围", fontSize = 12.sp, color = TextMuted)
                            Spacer(Modifier.height(12.dp))

                            // 自己
                            ViewModeItem("${currentUser.avatar} 我的记录", viewMode == "self", onClick = { onViewModeChange("self") })

                            // 全部
                            ViewModeItem("👥 全部用户", viewMode == "all", onClick = { onViewModeChange("all") })

                            // 各用户
                            allUsers.forEach { u ->
                                ViewModeItem("${u.avatar} ${u.name}", viewMode == u.userId, onClick = { onViewModeChange(u.userId) })
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }

        // 体重
        item {
            Surface(shape = RoundedCornerShape(20.dp), shadowElevation = 2.dp) {
                Column(Modifier.padding(24.dp)) {
                    Text("⚖ 体重设置", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            weightInput, { weightInput = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("输入体重") },
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            suffix = { Text("kg", color = TextMuted) }
                        )
                        Spacer(Modifier.width(12.dp))
                        Button(onClick = {
                            val w = weightInput.toIntOrNull() ?: 65
                            if (w in 30..200) onWeightSave(w)
                        }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                            Text("保存")
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // 数据管理
        item {
            Surface(shape = RoundedCornerShape(20.dp), shadowElevation = 2.dp) {
                Column(Modifier.padding(24.dp)) {
                    Text("🗄 数据管理", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("当前共有 $totalRecords 条运动记录", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(vertical = 8.dp))
                    OutlinedButton(onClick = onClearRecords,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Danger),
                        border = ButtonDefaults.outlinedButtonBorder
                    ) { Text("清除所有记录") }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // 关于
        item {
            Surface(shape = RoundedCornerShape(20.dp), shadowElevation = 2.dp) {
                Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏃‍♂️", fontSize = 40.sp)
                    Text("运动记录", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("计时 · MET × 体重(kg) × 时长(h)", fontSize = 12.sp, color = TextMuted)
                    Text("距离 · 距离(km) × 体重(kg) × 每km消耗", fontSize = 12.sp, color = TextMuted)
                    Text("计次 · 次数 × 单次消耗(千卡)", fontSize = 12.sp, color = TextMuted)
                    Text("v4.0.0 · Room数据库 · 多用户", fontSize = 11.sp, color = TextMuted)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ViewModeItem(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (selected) PrimaryLight else Color.Transparent,
        border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, Primary) else null
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = 14.sp)
            Spacer(Modifier.weight(1f))
            if (selected) Text("✓", color = Primary, fontWeight = FontWeight.Bold)
        }
    }
}
