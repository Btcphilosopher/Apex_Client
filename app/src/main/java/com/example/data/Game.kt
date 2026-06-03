package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class Game(
    @PrimaryKey val id: String,
    val title: String,
    val developer: String,
    val genre: String,
    val installSize: Long,
    val isInstalled: Boolean,
    val playtimeMinutes: Long,
    val achievements: List<String>,
    val version: String,
    val rating: Float,
    val price: Double,
    val tags: List<String>,
    val executableRef: String,
    val updateAvailable: Boolean
)
