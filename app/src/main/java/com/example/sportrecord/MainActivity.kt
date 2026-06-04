package com.example.sportrecord

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.sportrecord.data.entity.*
import com.example.sportrecord.model.*
import com.example.sportrecord.ui.add.AddScreen
import com.example.sportrecord.ui.cycling.CyclingScreen
import com.example.sportrecord.ui.cyclingdetail.CyclingDetailScreen
import com.example.sportrecord.ui.history.HistoryScreen
import com.example.sportrecord.ui.home.HomeScreen
import com.example.sportrecord.ui.login.LoginScreen
import com.example.sportrecord.ui.settings.SettingsScreen
import com.example.sportrecord.ui.theme.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val repo by lazy { SportRecordApplication.instance.repository }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 确保定位权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            activityResultRegistry.register("location", ActivityResultContracts.RequestMultiplePermissions()) {}.apply {
                launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }
        }

        setContent {
            SportRecordTheme {
                AppRoot()
            }
        }
    }

    @Composable
    fun AppRoot() {
        var screen by remember { mutableStateOf("login") }
        var recordIdForDetail by remember { mutableStateOf("") }
        val scope = rememberCoroutineScope()

        // 当前状态
        var currentUser by remember { mutableStateOf<User?>(null) }
        var appState by remember { mutableStateOf<AppState?>(null) }
        var users by remember { mutableStateOf<List<User>>(emptyList()) }
        var records by remember { mutableStateOf<List<SportRecord>>(emptyList()) }
        var viewLabel by remember { mutableStateOf("") }

        // 初始化：检查登录状态
        LaunchedEffect(Unit) {
            appState = repo.getState()
            users = repo.getUsers()
            val uid = appState?.currentUserId ?: ""
            currentUser = if (uid.isNotEmpty()) repo.getUser(uid) else null
            screen = if (currentUser != null) "home" else "login"
        }

        // 加载数据
        suspend fun refreshData() {
            appState = repo.getState()
            users = repo.getUsers()
            val uid = appState?.currentUserId ?: ""
            currentUser = if (uid.isNotEmpty()) repo.getUser(uid) else null

            val mode = appState?.adminViewMode ?: "self"
            records = repo.getRecordsByView(mode, uid)
            viewLabel = when (mode) {
                "self" -> ""
                "all" -> "全部用户"
                else -> repo.getUser(mode)?.name ?: mode
            }
        }

        LaunchedEffect(currentUser) { refreshData() }

        when (screen) {
            "login" -> {
                LoginScreen(
                    existingUsers = users.map { it.userId to (it.name to it.avatar) },
                    currentUserId = currentUser?.userId,
                    onCreateUser = { name, avatar ->
                        scope.launch {
                            val users = repo.getUsers()
                            val existing = users.find { it.name == name }
                            if (existing != null) {
                                repo.switchUser(existing.userId)
                            } else {
                                val uid = "user_${System.currentTimeMillis().toString(36)}"
                                repo.createUser(User(uid, name, avatar, isAdmin = users.isEmpty()))
                                repo.switchUser(uid)
                            }
                            refreshData()
                            screen = "home"
                        }
                    },
                    onSwitchUser = { uid ->
                        scope.launch { repo.switchUser(uid); refreshData(); screen = "home" }
                    }
                )
            }

            "add" -> {
                AddScreen(
                    weight = appState?.weight ?: 65,
                    onSave = { sport, date, dur, cnt, dist, cal ->
                        scope.launch {
                            val uid = currentUser?.userId ?: ""
                            val record = SportRecord(
                                id = repo.genId(),
                                userId = uid, sportType = sport.key, sportName = sport.name,
                                icon = sport.icon, duration = dur, count = cnt, distance = dist,
                                date = date, calories = cal, met = sport.met,
                                measureType = sport.measureType
                            )
                            repo.addRecord(record)
                            refreshData()
                            screen = "home"
                        }
                    },
                    onCycling = { screen = "cycling" },
                    onBack = { screen = "home" }
                )
            }

            "cycling" -> {
                CyclingScreen(
                    onSave = { tracker ->
                        scope.launch {
                            val uid = currentUser?.userId ?: ""
                            val stats = tracker.stats.value
                            val now = java.time.LocalDate.now().toString()
                            val record = SportRecord(
                                id = repo.genId(), userId = uid, sportType = "cycling",
                                sportName = "🚴 骑行", icon = "🚴",
                                duration = (stats.durationSec / 60).toInt(),
                                distance = stats.distKm, date = now,
                                calories = CalorieEngine.byDuration(6.0, appState?.weight ?: 65, (stats.durationSec / 60).toInt()),
                                met = 6.0, hasTrack = true
                            )
                            repo.addRecord(record)

                            // 保存轨迹
                            val track = Track(
                                recordId = record.id, userId = uid, date = now,
                                waypointsEncoded = tracker.waypoints.joinToString(";") { wp ->
                                    "${wp.lat},${wp.lng},${wp.alt.toInt()},${"%.1f".format(wp.segDist)}"
                                },
                                pointCount = stats.pointCount, totalDist = stats.distKm * 1000,
                                trackTime = stats.durationSec * 1000L,
                                avgSpeed = stats.avgSpeedKmh, maxSpeed = stats.maxSpeedKmh,
                                elevGain = stats.elevGain
                            )
                            repo.saveTrack(track)
                            refreshData()
                            screen = "home"
                        }
                    },
                    onBack = { screen = "home" }
                )
            }

            "detail" -> {
                val recordId = recordIdForDetail
                var track by remember { mutableStateOf<Track?>(null) }
                LaunchedEffect(recordId) {
                    track = repo.getTrack(recordId)
                }
                CyclingDetailScreen(track)
            }

            else -> {
                // 主界面：底部3Tab导航
                var tab by remember { mutableStateOf(0) }
                data class Tab(val emoji: String, val text: String)
                val tabs = listOf(Tab("🏃", "记录"), Tab("📊", "统计"), Tab("⚙", "设置"))

                Scaffold(
                    bottomBar = {
                        NavigationBar(containerColor = Surface) {
                            tabs.forEachIndexed { i, t ->
                                NavigationBarItem(
                                    selected = tab == i,
                                    onClick = { tab = i },
                                    icon = { Text(t.emoji, fontSize = 20.sp) },
                                    label = { Text(t.text, fontWeight = if (tab == i) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Primary, indicatorColor = PrimaryLight
                                    )
                                )
                            }
                        }
                    }
                ) { padding ->
                    Box(Modifier.padding(padding)) {
                        when (tab) {
                            0 -> HomeScreen(
                                currentUser, viewLabel, records,
                                onAdd = { screen = "add" },
                                onDelete = { id -> scope.launch { repo.deleteRecord(id); refreshData() } },
                                onTapRecord = { r ->
                                    if (r.hasTrack) {
                                        recordIdForDetail = r.id; screen = "detail"
                                    }
                                }
                            )
                            1 -> HistoryScreen(records)
                            2 -> SettingsScreen(
                                currentUser, appState?.weight ?: 65,
                                appState?.adminViewMode ?: "self", users, records.size,
                                onWeightSave = { w -> scope.launch { repo.setWeight(w); refreshData() } },
                                onSwitchUser = { scope.launch { repo.switchUser(""); refreshData(); screen = "login" } },
                                onViewModeChange = { mode -> scope.launch { repo.setAdminMode(mode); refreshData() } },
                                onClearRecords = {
                                    scope.launch { repo.deleteAllRecords(); refreshData() }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
