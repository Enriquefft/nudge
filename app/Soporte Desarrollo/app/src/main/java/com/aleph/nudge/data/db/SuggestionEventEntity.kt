package com.aleph.nudge.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suggestion_events")
data class SuggestionEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventType: String, // "shown", "accepted", "dismissed"
    val triggerItems: String, // JSON array of item names
    val suggestedItem: String,
    val suggestedItemId: String? = null,
    val priceCents: Long = 0,
    val synced: Boolean = false, // for backend sync
    val createdAt: Long = System.currentTimeMillis()
)
