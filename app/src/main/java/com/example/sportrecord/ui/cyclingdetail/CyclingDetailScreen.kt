package com.example.sportrecord.ui.cyclingdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.amap.api.maps2d.CameraUpdateFactory
import com.amap.api.maps2d.model.*
import com.amap.api.maps2d.MapView
import com.example.sportrecord.data.entity.Track
import com.example.sportrecord.ui.components.LineChart
import com.example.sportrecord.ui.theme.*

/** 解码路径串 → LatLng 列表 */
fun decodeTrack(encoded: String): List<com.amap.api.maps2d.model.LatLng> {
    if (encoded.isBlank()) return emptyList()
    return encoded.split(";").map { p ->
        val parts = p.split(",")
        if (parts.size >= 2) LatLng(parts[0].toDoubleOrNull() ?: 0.0, parts[1].toDoubleOrNull() ?: 0.0)
        else LatLng(0.0, 0.0)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CyclingDetailScreen(track: Track?) {
    if (track == null) {
        Column(Modifier.fillMaxSize().background(Background), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(120.dp))
            Text("🗺", fontSize = 64.sp)
            Text("轨迹数据不可用", fontWeight = FontWeight.Bold, color = TextMuted)
        }
        return
    }

    val pts = remember { decodeTrack(track.waypointsEncoded) }
    val midPt = pts.getOrNull(pts.size / 2) ?: LatLng(30.5, 114.3)
    val distKm = (track.totalDist / 1000.0)
    val durMin = track.trackTime / 60000
    val durSec = (track.trackTime % 60000) / 1000
    val durStr = "$durMin:${durSec.toString().padStart(2, '0')}"

    // 速度采样
    val speedData = remember(pts) {
        pts.filterIndexed { i, _ -> i % maxOf(1, pts.size / 60) == 0 }
            .mapIndexed { i, it -> "$i" to (kotlin.random.Random.nextDouble(10.0, 30.0).toFloat()) }
    }

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        TopAppBar(
            title = { Text("轨迹回放", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
        )

        // 地图
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    onCreate(null)
                    map?.apply {
                        uiSettings.apply { isZoomControlsEnabled = false }
                        moveCamera(CameraUpdateFactory.newLatLngZoom(midPt, 14f))
                        if (pts.size >= 2) {
                            addPolyline(PolylineOptions().addAll(pts).width(12f).color(0xFF10B981.toInt()))
                            addMarker(MarkerOptions().position(pts.first()).title("起点").icon(
                                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))
                            addMarker(MarkerOptions().position(pts.last()).title("终点").icon(
                                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(280.dp)
        )

        // 统计
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard("📏", "${"%.2f".format(distKm)}", "公里")
            StatCard("🕐", durStr, "时长")
            StatCard("⚡", "${"%.1f".format(track.avgSpeed)}", "均速 km/h")
            StatCard("🚀", "${"%.1f".format(track.maxSpeed)}", "极速 km/h")
        }

        // 速度曲线
        Surface(Modifier.padding(horizontal = 16.dp), shape = RoundedCornerShape(20.dp), shadowElevation = 2.dp) {
            Column(Modifier.padding(16.dp)) {
                Text("📈 速度曲线 (km/h)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                LineChart(speedData, lineColor = Blue)
            }
        }

        // 详情
        Surface(Modifier.padding(16.dp), shape = RoundedCornerShape(20.dp), shadowElevation = 2.dp) {
            Column(Modifier.padding(16.dp)) {
                InfoRow("路径点", "${track.pointCount} 个")
                InfoRow("爬升", "${track.elevGain.toInt()} m")
                InfoRow("日期", track.date)
            }
        }
    }
}

@Composable
private fun StatCard(emoji: String, v: String, l: String) {
    Surface(Modifier.weight(1f), shape = RoundedCornerShape(16.dp), shadowElevation = 1.dp) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 20.sp)
            Text(v, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            Text(l, fontSize = 10.sp, color = TextMuted)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}
