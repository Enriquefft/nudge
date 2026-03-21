package com.aleph.nudge.data

import com.aleph.nudge.data.db.DailyStatsEntity
import com.aleph.nudge.data.db.NudgeDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatsManager(private val db: NudgeDatabase) {

    private val statsDao = db.statsDao()

    private fun todayKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    suspend fun recordShown() = withContext(Dispatchers.IO) {
        statsDao.recordShown(todayKey())
    }

    suspend fun recordAccepted(priceInCents: Long) = withContext(Dispatchers.IO) {
        statsDao.recordAccepted(todayKey(), priceInCents)
    }

    suspend fun recordDismissed() = withContext(Dispatchers.IO) {
        statsDao.recordDismissed(todayKey())
    }

    suspend fun getTodayShown(): Int = withContext(Dispatchers.IO) {
        statsDao.getByDate(todayKey())?.shown ?: 0
    }

    suspend fun getTodayAccepted(): Int = withContext(Dispatchers.IO) {
        statsDao.getByDate(todayKey())?.accepted ?: 0
    }

    suspend fun getTodayDismissed(): Int = withContext(Dispatchers.IO) {
        statsDao.getByDate(todayKey())?.dismissed ?: 0
    }

    suspend fun getTodayRevenue(): Long = withContext(Dispatchers.IO) {
        statsDao.getByDate(todayKey())?.revenueCents ?: 0L
    }

    suspend fun getAcceptanceRate(): Float {
        val shown = getTodayShown()
        if (shown == 0) return 0f
        return getTodayAccepted().toFloat() / shown.toFloat() * 100f
    }

    suspend fun getTodayRevenueFormatted(): String {
        val cents = getTodayRevenue()
        return "$${cents / 100}.${"%02d".format(cents % 100)}"
    }

    /**
     * Returns true if demo data has already been seeded for today.
     * Uses runBlocking because it is called from Application.onCreate().
     */
    fun hasDemoData(): Boolean = runBlocking(Dispatchers.IO) {
        (statsDao.getByDate(todayKey())?.shown ?: 0) > 0
    }

    /**
     * Pre-seeds realistic stats for a busy morning demo shift.
     * Uses runBlocking because it is called from Application.onCreate().
     */
    fun seedDemoData() = runBlocking(Dispatchers.IO) {
        statsDao.upsert(
            DailyStatsEntity(
                date = todayKey(),
                shown = 47,
                accepted = 28,
                dismissed = 6,
                revenueCents = 8450L
            )
        )
    }
}
