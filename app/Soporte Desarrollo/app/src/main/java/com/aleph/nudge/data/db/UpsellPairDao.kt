package com.aleph.nudge.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UpsellPairDao {

    @Query("SELECT * FROM upsell_pairs WHERE pairKey = :key")
    fun getByKey(key: String): UpsellPairEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(pair: UpsellPairEntity)

    @Query("SELECT * FROM upsell_pairs")
    fun getAll(): List<UpsellPairEntity>

    @Query("SELECT COUNT(*) FROM upsell_pairs")
    fun count(): Int

    @Query("DELETE FROM upsell_pairs WHERE pairKey IN (SELECT pairKey FROM upsell_pairs ORDER BY lastUpdated ASC LIMIT :count)")
    fun deleteOldest(count: Int)
}
