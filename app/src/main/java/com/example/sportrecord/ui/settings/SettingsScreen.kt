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
import com.example.sportrecord.model.SportsData
import com.example.sportrecord.model.SportDef
import com.example.sportrecord.ui.theme.*
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentUser: User?,
    weight: Int,
    viewMode: String,
    allUsers: List<User>,
    totalRecords: Int,
    customSports: List<SportDef>,
    onWeightSave: (Int) -> Unit,
    onSwitchUser: () -> Unit,
    onViewModeChange: (String) -> Unit,
    onClearRecords: () -> Unit,
    onEditSport: (SportDef, String, String, String, String) -> Unit,
    onResetSport: (SportDef) -> Unit,
    onAddCustomSport: (String, String, String, String, String) -> Unit,
    onDeleteCustomSport: (SportDef) -> Unit
) {
    var weightInput by remember { mutableStateOf(weight.toString()) }

    // 编辑弹窗状态
    var editSport by remember { mutableStateOf<SportDef?>(null) }
    var editMet by remember { mutableStateOf("") }
    var editMeasureType by remember { mutableStateOf("") }
    var editKcalPerUnit by remember { mutableStateOf("") }
    var editCaloriePerKm by remember { mutableStateOf("") }

    // 自定义运动表单
    var customName by remember { mutableStateOf("") }
    var customIcon by remember { mutableStateOf("") }
    var customMet by remember { mutableStateOf("") }
    var customMeasureType by remember { mutableStateOf("count") }
    var customKcalPerUnit by remember { mutableStateOf("") }

    val allSports = remember { SportsData.allSports + customSports }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Background),
        contentPadding = PaddingValues(24.dp)
    ) {
        // 用户
        if (currentUser != null) {
            item { UserCard(currentUser, onSwitchUser) }
            if (currentUser.isAdmin) {
                item { AdminPanel(viewMode, allUsers, currentUser, onViewModeChange) }
            }
        }

        // 体重
        item { WeightCard(weightInput, { weightInput = it }, { onWeightSave(it) }) }

        // 运动类型管理 + 编辑
        item {
            Surface(shape = RoundedCornerShape(20.dp), shadowElevation = 2.dp) {
                Column(Modifier.padding(20.dp)) {
                    Text("📋 运动类型管理", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("点击编辑可修改 MET 值、计量方式等参数", fontSize = 12.sp, color = TextMuted)
                    Spacer(Modifier.height(8.dp))
                    allSports.forEach { sport ->
                        SportRow(
                            sport = sport,
                            onEdit = {
                                editSport = sport
                                editMet = sport.met.toString()
                                editMeasureType = sport.measureType
                                editKcalPerUnit = sport.kcalPerUnit.toString()
                                editCaloriePerKm = sport.caloriePerKm.toString()
                            },
                            onDelete = if (sport.key.startsWith("custom_")) ({ onDeleteCustomSport(sport) }) else null,
                            onReset = if (!sport.key.startsWith("custom_")) ({
                                onResetSport(sport)
                            }) else null
                        )
                        Divider(color = Color(0xFFF1F5F9))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // 添加自定义运动
        item { CustomSportCard(customName, customIcon, customMet, customMeasureType, customKcalPerUnit,
            onNameChange = { customName = it }, onIconChange = { customIcon = it },
            onMetChange = { customMet = it }, onMeasureTypeChange = { customMeasureType = it },
            onKcalChange = { customKcalPerUnit = it },
            onAdd = {
                onAddCustomSport(customName.trim(), customIcon.trim(), customMet, customMeasureType, customKcalPerUnit)
                customName = ""; customIcon = ""; customMet = ""; customKcalPerUnit = ""
            }
        ) }

        // 数据管理
        item { DataCard(totalRecords, onClearRecords) }

        // 关于
        item { AboutCard() }
        item { Spacer(Modifier.height(32.dp)) }
    }

    // 编辑弹窗
    if (editSport != null) {
        val s = editSport!!
        AlertDialog(
            onDismissRequest = { editSport = null },
            shape = RoundedCornerShape(24.dp),
            title = { Text("编辑「${s.name.replace(Regex("[^一-龥]"), "")}」", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(editMet, { editMet = it }, label = { Text("MET 值") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    Spacer(Modifier.height(8.dp))
                    Text("计量方式", fontSize = 13.sp, color = TextSecondary)
                    Row(Modifier.fillMaxWidth()) {
                        listOf("duration" to "计时", "count" to "计次", "both" to "两者").forEach { (k, v) ->
                            FilterChip(selected = editMeasureType == k, onClick = { editMeasureType = k },
                                label = { Text(v) }, modifier = Modifier.padding(2.dp))
                        }
                    }
                    if (editMeasureType == "count" || editMeasureType == "both") {
                        OutlinedTextField(editKcalPerUnit, { editKcalPerUnit = it }, label = { Text("单次消耗(千卡)") },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                        Spacer(Modifier.height(8.dp))
                    }
                    OutlinedTextField(editCaloriePerKm, { editCaloriePerKm = it }, label = { Text("每km消耗(0=不支持)") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                }
            },
            confirmButton = {
                Button(onClick = {
                    onEditSport(s, editMet, editMeasureType, editKcalPerUnit, editCaloriePerKm)
                    editSport = null
                }, colors = ButtonDefaults.buttonColors(containerColor = Primary)) { Text("保存") }
            },
            dismissButton = { TextButton(onClick = { editSport = null }) { Text("取消") } }
        )
    }
}

// ── 子组件 ──

@Composable
private fun UserCard(user: User, onSwitch: () -> Unit) {
    Surface(shape = RoundedCornerShape(20.dp), shadowElevation = 2.dp) {
        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(52.dp).background(Brush.linearGradient(listOf(PrimaryLight, Color(0xFFA7F3D0))), RoundedCornerShape(26.dp)),
                contentAlignment = Alignment.Center) { Text(user.avatar, fontSize = 24.sp) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(user.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(if (user.isAdmin) "管理员" else "普通用户", fontSize = 12.sp,
                    color = if (user.isAdmin) Amber else TextMuted)
            }
            OutlinedButton(onClick = onSwitch) { Text("切换") }
        }
    }
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun AdminPanel(viewMode: String, users: List<User>, me: User, onChange: (String) -> Unit) {
    Surface(shape = RoundedCornerShape(20.dp), shadowElevation = 2.dp) {
        Column(Modifier.padding(20.dp)) {
            Text("🛡 管理员面板", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            ViewModeItem("${me.avatar} 我的记录", viewMode == "self") { onChange("self") }
            ViewModeItem("👥 全部用户", viewMode == "all") { onChange("all") }
            users.forEach { u ->
                ViewModeItem("${u.avatar} ${u.name}", viewMode == u.userId) { onChange(u.userId) }
            }
        }
    }
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun ViewModeItem(label: String, sel: Boolean, onClick: () -> Unit) {
    Surface(Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (sel) PrimaryLight else Color.Transparent,
        border = if (sel) androidx.compose.foundation.BorderStroke(1.dp, Primary) else null) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = 14.sp); Spacer(Modifier.weight(1f))
            if (sel) Text("✓", color = Primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun WeightCard(value: String, onValue: (String) -> Unit, onSave: (Int) -> Unit) {
    Surface(shape = RoundedCornerShape(20.dp), shadowElevation = 2.dp) {
        Column(Modifier.padding(20.dp)) {
            Text("⚖ 体重设置", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("用于计算运动消耗热量", fontSize = 12.sp, color = TextMuted)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value, onValue, modifier = Modifier.weight(1f), placeholder = { Text("体重") },
                    shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("kg", color = TextMuted) })
                Spacer(Modifier.width(12.dp))
                Button(onClick = { val w = value.toIntOrNull() ?: 90; if (w in 30..200) onSave(w) },
                    shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) { Text("保存") }
            }
        }
    }
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun SportRow(sport: SportDef, onEdit: () -> Unit, onDelete: (() -> Unit)?, onReset: (() -> Unit)?) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).background(PrimaryLight, RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
            Text(sport.icon, fontSize = 18.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(sport.name.replace(Regex("[^一-龥]"), ""), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text("MET ${sport.met} · ${when(sport.measureType){"duration"->"计时";"count"->"计次";else->"计时+计次"}}",
                fontSize = 11.sp, color = TextMuted)
        }
        if (onDelete != null) {
            TextButton(onClick = onDelete) { Text("删除", color = Danger, fontSize = 12.sp) }
        }
        if (onReset != null) {
            TextButton(onClick = onReset) { Text("恢复", color = TextMuted, fontSize = 12.sp) }
        }
        TextButton(onClick = onEdit) { Text("编辑", color = Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomSportCard(
    name: String, icon: String, met: String, measureType: String, kcal: String,
    onNameChange: (String) -> Unit, onIconChange: (String) -> Unit,
    onMetChange: (String) -> Unit, onMeasureTypeChange: (String) -> Unit,
    onKcalChange: (String) -> Unit, onAdd: () -> Unit
) {
    Surface(shape = RoundedCornerShape(20.dp), shadowElevation = 2.dp) {
        Column(Modifier.padding(20.dp)) {
            Text("✨ 添加自定义运动", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(name, onNameChange, modifier = Modifier.fillMaxWidth(), placeholder = { Text("运动名称, 如: 平板支撑") },
                shape = RoundedCornerShape(12.dp))
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(icon, onIconChange, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Emoji图标, 如: 🧘") },
                shape = RoundedCornerShape(12.dp), singleLine = true)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(met, onMetChange, modifier = Modifier.weight(1f), placeholder = { Text("MET 如: 3.0") },
                    shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("计量方式", fontSize = 12.sp, color = TextMuted)
                    Row {
                        listOf("duration" to "计时", "count" to "计次", "both" to "两者").forEach { (k, v) ->
                            FilterChip(selected = measureType == k, onClick = { onMeasureTypeChange(k) },
                                label = { Text(v, fontSize = 11.sp) }, modifier = Modifier.padding(end = 4.dp))
                        }
                    }
                }
            }
            if (measureType == "count" || measureType == "both") {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(kcal, onKcalChange, modifier = Modifier.fillMaxWidth(), placeholder = { Text("单次消耗(千卡), 如: 0.5") },
                    shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onAdd, enabled = name.isNotBlank() && icon.isNotBlank() && met.toDoubleOrNull() != null,
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)) { Text("+ 添加运动", fontWeight = FontWeight.Bold) }
        }
    }
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun DataCard(totalRecords: Int, onClear: () -> Unit) {
    Surface(shape = RoundedCornerShape(20.dp), shadowElevation = 2.dp) {
        Column(Modifier.padding(20.dp)) {
            Text("🗄 数据管理", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("当前共有 $totalRecords 条运动记录", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(vertical = 8.dp))
            OutlinedButton(onClick = onClear, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Danger)) { Text("清除所有记录") }
        }
    }
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun AboutCard() {
    Surface(shape = RoundedCornerShape(20.dp), shadowElevation = 2.dp) {
        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🏃‍♂️", fontSize = 40.sp)
            Text("运动记录", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("计时 · MET × 体重(kg) × 时长(h)", fontSize = 12.sp, color = TextMuted)
            Text("距离 · 距离(km) × 体重(kg) × 每km消耗", fontSize = 12.sp, color = TextMuted)
            Text("计次 · 次数 × 单次消耗(千卡)", fontSize = 12.sp, color = TextMuted)
            Text("v4.0.0", fontSize = 11.sp, color = TextMuted)
        }
    }
}
