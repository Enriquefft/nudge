package com.aleph.nudge.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_stats")
data class DailyStatsEntity(
    @PrimaryKey val date: String, // "yyyy-MM-dd"
    val shown: Int = 0,
    val accepted: Int = 0,
    val dismissed: Int = 0,
    val revenueCents: Long = 0L
)
