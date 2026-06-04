package com.example.sportrecord.tracker

import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import kotlinx.coroutines.*
import kotlin.math.*

/**
 * GPS 轨迹追踪引擎
 *
 * 使用 Android LocationManager 进行持续定位。
 * Haversine 距离、速度、海拔计算。
 */
class GpsTracker(private val locationProvider: suspend () -> Location?) {

    enum class TrackerState { IDLE, TRACKING, PAUSED }

    // ── 实时统计 ──
    data class Stats(
        val trackerState: TrackerState = TrackerState.IDLE,
        val distKm: Double = 0.0,
        val durationSec: Long = 0,
        val curSpeedKmh: Double = 0.0,
        val avgSpeedKmh: Double = 0.0,
        val maxSpeedKmh: Double = 0.0,
        val elevGain: Double = 0.0,
        val pointCount: Int = 0
    ) {
        val durationStr: String get() {
            val h = durationSec / 3600; val m = (durationSec % 3600) / 60; val s = durationSec % 60
            return if (h > 0) "%d:%02d:%02d".format(h,m,s) else "%02d:%02d".format(m,s)
        }
    }

    private val _stats = mutableStateOf(Stats())
    val stats: State<Stats> = _stats

    // 路径点
    data class Waypoint(
        val lat: Double, val lng: Double,
        val alt: Double, val segDist: Double, val ts: Long
    )
    private val _waypoints = mutableListOf<Waypoint>()
    val waypoints: List<Waypoint> get() = _waypoints

    private var job: Job? = null
    private var lastPt: Waypoint? = null
    private var lastAlt: Double? = null
    private var startTs = 0L
    private var pausedMs = 0L
    private var pauseStart = 0L
    private var activeMs = 0L

    // ── 开始 ──
    fun start(scope: CoroutineScope) {
        reset()
        _stats.value = _stats.value.copy(trackerState = TrackerState.TRACKING)
        startTs = System.currentTimeMillis()

        job = scope.launch(Dispatchers.IO) {
            while (isActive && _stats.value.trackerState != TrackerState.IDLE) {
                if (_stats.value.trackerState == TrackerState.TRACKING) {
                    val loc = locationProvider()
                    if (loc != null) handleLocation(loc)
                }
                delay(2000) // 2s 间隔
            }
        }
    }

    // ── 暂停 / 恢复 ──
    fun pause() {
        if (_stats.value.trackerState != TrackerState.TRACKING) return
        _stats.value = _stats.value.copy(trackerState = TrackerState.PAUSED)
        pauseStart = System.currentTimeMillis()
    }
    fun resume() {
        if (_stats.value.trackerState != TrackerState.PAUSED) return
        pausedMs += System.currentTimeMillis() - pauseStart
        _stats.value = _stats.value.copy(trackerState = TrackerState.TRACKING)
    }

    // ── 结束 ──
    fun stop(): Stats {
        if (_stats.value.trackerState == TrackerState.PAUSED) pausedMs += System.currentTimeMillis() - pauseStart
        job?.cancel()
        _stats.value = _stats.value.copy(trackerState = TrackerState.IDLE)
        return _stats.value
    }

    // ── 内部 ──
    private fun reset() {
        _waypoints.clear(); lastPt = null; lastAlt = null
        startTs = 0; pausedMs = 0; activeMs = 0
    }

    @SuppressLint("MissingPermission")
    private fun handleLocation(loc: Location) {
        val lat = loc.latitude; val lng = loc.longitude
        val alt = loc.altitude; val acc = loc.accuracy

        // 过滤低精度
        if (!acc.isNaN() && acc > 30) return

        val now = System.currentTimeMillis()
        var segDist = 0.0
        if (lastPt != null) {
            segDist = haversine(lastPt!!.lat, lastPt!!.lng, lat, lng)
            // 过滤飞跃
            if (segDist > 200 && acc > 15) return

            val dtMs = now - lastPt!!.ts
            val kmh = if (dtMs > 0 && segDist > 0) (segDist / dtMs) * 3600.0 else 0.0

            // 运动时间
            if (segDist < 1 && dtMs <= 10000) activeMs += dtMs
            else if (dtMs in 1..30000) activeMs += dtMs

            // 更新极速
            if (kmh in 0.1..80.0) {
                val s = _stats.value
                if (kmh > s.maxSpeedKmh) _stats.value = s.copy(maxSpeedKmh = kmh)
                _stats.value = _stats.value.copy(curSpeedKmh = kotlin.math.round(kmh * 10) / 10.0)
            }
        }

        // 海拔
        var elevGain = _stats.value.elevGain
        if (lastAlt != null && alt > lastAlt!!) elevGain += alt - lastAlt!!

        // 存点
        val wp = Waypoint(lat, lng, alt, segDist, now)
        _waypoints.add(wp)
        lastPt = wp; lastAlt = alt

        // 刷新统计
        val totalDist = _waypoints.sumOf { it.segDist }
        val activeSec = max(0, activeMs) / 1000
        val avgSpd = if (activeSec > 0) (totalDist / activeMs) * 3600.0 else 0.0

        _stats.value = Stats(
            trackerState = TrackerState.TRACKING,
            distKm = kotlin.math.round(totalDist / 10.0) / 100.0, // 米→km, 保留2位
            durationSec = activeSec,
            curSpeedKmh = _stats.value.curSpeedKmh,
            avgSpeedKmh = kotlin.math.round(avgSpd * 10) / 10.0,
            maxSpeedKmh = _stats.value.maxSpeedKmh,
            elevGain = kotlin.math.round(elevGain).toDouble(),
            pointCount = _waypoints.size
        )
    }

    companion object {
        private const val EARTH_R = 6371000.0
        fun haversine(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
            val dLat = Math.toRadians(lat2 - lat1)
            val dLng = Math.toRadians(lng2 - lng1)
            val a = sin(dLat/2)*sin(dLat/2) + cos(Math.toRadians(lat1))*cos(Math.toRadians(lat2))*sin(dLng/2)*sin(dLng/2)
            return EARTH_R * 2 * atan2(sqrt(a), sqrt(1 - a))
        }
    }
}
