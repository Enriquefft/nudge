package com.aleph.nudge.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DailyStatsEntity::class, UpsellPairEntity::class, SuggestionEventEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NudgeDatabase : RoomDatabase() {

    abstract fun statsDao(): StatsDao
    abstract fun upsellPairDao(): UpsellPairDao
    abstract fun suggestionEventDao(): SuggestionEventDao

    companion object {
        @Volatile
        private var INSTANCE: NudgeDatabase? = null

        fun getInstance(context: Context): NudgeDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    NudgeDatabase::class.java,
                    "nudge.db"
                )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
