package com.example.sportrecord.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sportrecord.data.entity.SportRecord
import com.example.sportrecord.ui.theme.*

@Composable
fun RecordCard(
    record: SportRecord,
    onDelete: () -> Unit,
    onTap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onTap),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp,
        color = Surface
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(PrimaryLight, Color(0xFFA7F3D0)))),
                contentAlignment = Alignment.Center
            ) { Text(record.icon, fontSize = 24.sp) }

            Spacer(Modifier.width(16.dp))

            // 内容
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(record.sportName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    if (record.hasTrack) {
                        Text(" 🗺️", fontSize = 14.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(record.date, fontSize = 12.sp, color = TextMuted)
                }
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (record.duration > 0) MetaChip("🕐 ${record.duration}分钟")
                    if (record.distance > 0) MetaChip("📏 ${record.distance}km")
                    if (record.count > 0) MetaChip("🔢 ${record.count}次")
                }
            }

            // 卡路里
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${record.calories}", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Primary)
                Text("千卡", fontSize = 11.sp, color = TextMuted)
            }

            Spacer(Modifier.width(8.dp))

            // 删除
            Text("×", fontSize = 18.sp, color = TextMuted, modifier = Modifier.clickable { onDelete() })
        }
    }
}

@Composable
private fun MetaChip(text: String) {
    Text(text, fontSize = 12.sp, color = TextSecondary,
        modifier = Modifier
            .background(Background, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 2.dp))
}
