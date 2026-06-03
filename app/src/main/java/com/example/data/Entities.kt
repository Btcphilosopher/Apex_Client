package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSetting(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "download_jobs")
data class DownloadJob(
    @PrimaryKey val gameId: String,
    val title: String,
    val status: String, // "PENDING", "DOWNLOADING", "PAUSED", "COMPLETED", "FAILED"
    val progress: Float, // 0.0f to 1.0f
    val currentBytes: Long,
    val totalBytes: Long,
    val etaSeconds: Int = 0
)

@Entity(tableName = "friend_activities")
data class FriendActivity(
    @PrimaryKey val friendName: String,
    val status: String, // "online", "offline", "in-game"
    val currentGame: String? = null,
    val avatarColorHex: String = "#00FFCC",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "client_alerts")
data class ClientAlert(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
