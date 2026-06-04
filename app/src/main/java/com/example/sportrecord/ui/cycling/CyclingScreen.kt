package com.example.sportrecord.ui.cycling

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.amap.api.maps2d.AMap
import com.amap.api.maps2d.CameraUpdateFactory
import com.amap.api.maps2d.model.*
import com.amap.api.maps2d.MapView
import com.example.sportrecord.tracker.GpsTracker
import com.example.sportrecord.ui.theme.*
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.*
import android.view.View
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.coroutines.resume
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import android.annotation.SuppressLint

@Composable
fun CyclingScreen(
    onSave: (tracker: GpsTracker) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lm = remember { context.getSystemService<LocationManager>() }
    val tracker = remember {
        GpsTracker {
            try {
                @Suppress("DEPRECATION")
                suspendCoroutine<Location?> { cont ->
                    val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
                    var best: Location? = null
                    for (p in providers) {
                        try { best = lm?.getLastKnownLocation(p); if (best != null) break } catch (_: Exception) {}
                    }
                    if (best != null) cont.resume(best)
                    else lm?.requestSingleUpdate(LocationManager.GPS_PROVIDER, { loc -> cont.resume(loc) }, null)
                    ?: cont.resume(null)
                }
            } catch (_: Exception) { null }
        }
    }
    val stats by tracker.stats
    var hasStarted by remember { mutableStateOf(false) }
    var showFinish by remember { mutableStateOf(false) }
    var gpsWeak by remember { mutableStateOf(false) }

    // 权限检查
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
        hasPermission = perms.values.all { it }
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    // 地图
    var aMap by remember { mutableStateOf<AMap?>(null) }
    val polylineRef = remember { mutableListOf<LatLng>() }

    Box(modifier = Modifier.fillMaxSize()) {
        // 地图
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    onCreate(null)
                    aMap = this.map
                    aMap?.uiSettings?.apply {
                        isZoomControlsEnabled = false
                        isMyLocationButtonEnabled = true
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { mapView ->
                val wps = tracker.waypoints
                if (wps.isNotEmpty()) {
                    val pts = wps.map { LatLng(it.lat, it.lng) }
                    polylineRef.clear(); polylineRef.addAll(pts)
                    aMap?.clear()
                    if (pts.size >= 2) {
                        aMap?.addPolyline(PolylineOptions().addAll(pts).width(12f).color(0xFF10B981.toInt()))
                    }
                    val last = pts.last()
                    aMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(last, 17f))
                }
            }
        )

        // 顶部栏
        Surface(
            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).statusBarsPadding(),
            color = Surface.copy(alpha = 0.9f)
        ) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("←", fontSize = 22.sp, modifier = Modifier.clickable {
                    if (hasStarted && stats.trackerState != GpsTracker.TrackerState.IDLE) {
                        showFinish = true
                    } else onBack()
                })
                Spacer(Modifier.width(12.dp))
                Text("骑行记录", fontWeight = FontWeight.Bold)
            }
        }

        // 数据面板
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 60.dp, start = 16.dp, end = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = Surface.copy(alpha = 0.92f),
            shadowElevation = 8.dp
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatCol(stats.durationStr, "时长")
                    StatCol("${stats.curSpeedKmh}", "km/h", big = true)
                    StatCol("${stats.distKm}", "公里")
                }
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatCol("${stats.elevGain.toInt()}m", "爬升", mini = true)
                    StatCol("${stats.maxSpeedKmh}", "极速", mini = true)
                    StatCol("${stats.avgSpeedKmh}", "均速", mini = true)
                    StatCol("${stats.pointCount}", "点", mini = true)
                }
            }
        }

        // 控制栏
        Row(
            Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!hasStarted || stats.trackerState == GpsTracker.TrackerState.IDLE) {
                Button(
                    onClick = { hasStarted = true; tracker.start(scope) },
                    modifier = Modifier.height(60.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    enabled = hasPermission
                ) {
                    Text("▶ 开始骑行", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                // 结束
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FilledTonalButton(
                        onClick = {
                            tracker.stop()
                            showFinish = true
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0x66000000))
                    ) { Text("■ 结束", color = Surface) }
                }
                Spacer(Modifier.width(32.dp))

                // 暂停/恢复
                if (stats.trackerState == GpsTracker.TrackerState.TRACKING) {
                    FloatingActionButton(
                        onClick = { tracker.pause() },
                        modifier = Modifier.size(72.dp),
                        containerColor = Amber
                    ) { Text("⏸", fontSize = 24.sp) }
                } else if (stats.trackerState == GpsTracker.TrackerState.PAUSED) {
                    FloatingActionButton(
                        onClick = { tracker.resume() },
                        modifier = Modifier.size(72.dp),
                        containerColor = Primary
                    ) { Text("▶", fontSize = 24.sp) }
                }
            }
        }

        // GPS 弱信号警告
        if (gpsWeak) {
            Surface(
                Modifier.fillMaxWidth().padding(24.dp).align(Alignment.BottomCenter).padding(bottom = 100.dp),
                shape = RoundedCornerShape(16.dp),
                color = Danger.copy(alpha = 0.85f)
            ) { Text("⚠ GPS信号弱，请移至开阔处", color = Surface, fontSize = 14.sp, modifier = Modifier.padding(12.dp)) }
        }
    }

    // 结束确认弹窗
    if (showFinish) {
        val s = stats
        AlertDialog(
            onDismissRequest = { showFinish = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text("结束骑行？", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("总距离"); Text("${s.distKm} km") }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("运动时长"); Text(s.durationStr) }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("平均速度"); Text("${s.avgSpeedKmh} km/h") }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("极速"); Text("${s.maxSpeedKmh} km/h") }
                }
            },
            confirmButton = {
                Button(onClick = { showFinish = false; onSave(tracker) }, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                    Text("✓ 保存记录")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showFinish = false
                    hasStarted = false
                }) { Text("放弃") }
            }
        )
    }
}

@Composable
private fun StatCol(value: String, label: String, big: Boolean = false, mini: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = if (big) 32.sp else if (mini) 14.sp else 22.sp,
            fontWeight = if (big) FontWeight.Black else FontWeight.Bold,
            color = if (big) Primary else TextPrimary)
        Text(label, fontSize = if (mini) 10.sp else 12.sp, color = TextMuted)
    }
}


