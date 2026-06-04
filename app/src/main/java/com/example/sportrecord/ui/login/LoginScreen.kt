package com.example.sportrecord.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sportrecord.ui.theme.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    existingUsers: List<Pair<String/*id*/, Pair<String/*name*/, String/*avatar*/>>>,
    currentUserId: String?,
    onCreateUser: (String, String) -> Unit,
    onSwitchUser: (String) -> Unit
) {
    val avatars = listOf("🏃","🚴","🏊","🧘","⚽","🏀","🎾","🥾","💪","🔥","🌟")
    var nickname by remember { mutableStateOf("") }
    var avatarIdx by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFECFDF5), Color(0xFFF0F9FF))))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(80.dp))

        Text("🏃‍♂️", fontSize = 56.sp)
        Text("运动记录", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        Text("记录每一次运动，见证每一步成长", fontSize = 14.sp, color = TextMuted)

        Spacer(Modifier.height(48.dp))

        // 头像
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(PrimaryLight, Color(0xFFA7F3D0))))
                .clickable { avatarIdx = (avatarIdx + 1) % avatars.size },
            contentAlignment = Alignment.Center
        ) { Text(avatars[avatarIdx], fontSize = 44.sp) }
        Text("点击更换头像", fontSize = 12.sp, color = TextMuted)

        Spacer(Modifier.height(32.dp))

        // 昵称
        Text("你的昵称", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = nickname, onValueChange = { nickname = it },
            placeholder = { Text("输入昵称", color = TextMuted) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Color(0xFFE2E8F0))
        )

        Spacer(Modifier.height(24.dp))

        // 已有用户
        if (existingUsers.isNotEmpty()) {
            Text("切换用户", fontSize = 13.sp, color = TextMuted, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                existingUsers.forEach { (uid, pair) ->
                    FilterChip(
                        selected = uid == currentUserId,
                        onClick = { onSwitchUser(uid) },
                        label = { Text("${pair.second} ${pair.first}") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryLight)
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        // 按钮
        Button(
            onClick = { onCreateUser(nickname.trim(), avatars[avatarIdx]) },
            enabled = nickname.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) { Text("✓ 开始记录", fontSize = 16.sp, fontWeight = FontWeight.Bold) }

        Spacer(Modifier.height(16.dp))
        Text("第一个用户自动成为管理员", fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.Center)
    }
}
