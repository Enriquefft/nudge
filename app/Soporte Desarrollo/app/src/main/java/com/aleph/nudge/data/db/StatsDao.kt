package com.aleph.nudge.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface StatsDao {

    @Query("SELECT * FROM daily_stats WHERE date = :date")
    fun getByDate(date: String): DailyStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(stats: DailyStatsEntity)

    @Query("UPDATE daily_stats SET shown = shown + 1 WHERE date = :date")
    fun incrementShown(date: String)

    @Query("UPDATE daily_stats SET accepted = accepted + 1, revenueCents = revenueCents + :cents WHERE date = :date")
    fun incrementAccepted(date: String, cents: Long)

    @Query("UPDATE daily_stats SET dismissed = dismissed + 1 WHERE date = :date")
    fun incrementDismissed(date: String)

    @Transaction
    fun recordShown(date: String) {
        if (getByDate(date) == null) upsert(DailyStatsEntity(date = date))
        incrementShown(date)
    }

    @Transaction
    fun recordAccepted(date: String, cents: Long) {
        if (getByDate(date) == null) upsert(DailyStatsEntity(date = date))
        incrementAccepted(date, cents)
    }

    @Transaction
    fun recordDismissed(date: String) {
        if (getByDate(date) == null) upsert(DailyStatsEntity(date = date))
        incrementDismissed(date)
    }
}
