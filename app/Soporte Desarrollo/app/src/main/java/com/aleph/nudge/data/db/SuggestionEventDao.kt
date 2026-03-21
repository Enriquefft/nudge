package com.aleph.nudge.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SuggestionEventDao {

    @Insert
    fun insert(event: SuggestionEventEntity)

    @Query("SELECT * FROM suggestion_events WHERE synced = 0 ORDER BY createdAt ASC LIMIT :limit")
    fun getUnsynced(limit: Int = 100): List<SuggestionEventEntity>

    @Query("UPDATE suggestion_events SET synced = 1 WHERE id IN (:ids)")
    fun markSynced(ids: List<Long>)
}
