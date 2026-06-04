package com.example.sportrecord.data.entity

import androidx.room.*

// ==================== 用户 ====================
@Entity(tableName = "users")
data class User(
    @PrimaryKey val userId: String,
    val name: String = "用户",
    val avatar: String = "🏃",   // 🏃
    val isAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// ==================== 运动记录 ====================
@Entity(tableName = "records", indices = [Index("date"), Index("userId")])
data class SportRecord(
    @PrimaryKey val id: String,
    val userId: String = "",
    val sportType: String,
    val sportName: String,
    val icon: String = "🏃",
    val duration: Int = 0,          // 分钟
    val count: Int = 0,             // 次数
    val distance: Double = 0.0,     // 公里
    val date: String,                // "2026-06-04"
    val calories: Int = 0,
    val met: Double = 1.0,
    val measureType: String = "duration",
    val hasTrack: Boolean = false,   // 是否有轨迹
    val createdAt: Long = System.currentTimeMillis()
)

// ==================== 轨迹 ====================
@Entity(tableName = "tracks", indices = [Index("recordId", unique = true)])
data class Track(
    @PrimaryKey(autoGenerate = true) val _id: Long = 0,
    val recordId: String,
    val userId: String = "",
    val date: String = "",
    val waypointsEncoded: String = "",   // "lat,lng,alt,segDist;..."
    val pointCount: Int = 0,
    val totalDist: Double = 0.0,         // 米
    val trackTime: Long = 0,             // ms
    val avgSpeed: Double = 0.0,          // km/h
    val maxSpeed: Double = 0.0,          // km/h
    val elevGain: Double = 0.0,          // 米
    val createdAt: Long = System.currentTimeMillis()
)

// ==================== 设置 ====================
@Entity(tableName = "app_state")
data class AppState(
    @PrimaryKey val key: String = "state",
    val currentUserId: String = "",
    val weight: Int = 65,
    val adminViewMode: String = "self"   // "self" | "all" | userId
)
