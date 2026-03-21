package com.aleph.nudge.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "upsell_pairs")
data class UpsellPairEntity(
    @PrimaryKey val pairKey: String, // "trigger\u001Fsuggested" (lowercase)
    val triggerItem: String,
    val suggestedItem: String,
    val accepted: Int = 0,
    val dismissed: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)
