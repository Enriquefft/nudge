package com.aleph.nudge.data

import android.content.Context

class StatsManager(context: Context) {

    private val prefs = context.getSharedPreferences("nudge_stats", Context.MODE_PRIVATE)

    private fun todayKey(suffix: String): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        return "${sdf.format(java.util.Date())}_$suffix"
    }

    fun recordShown() {
        increment(todayKey("shown"))
    }

    fun recordAccepted(priceInCents: Long) {
        increment(todayKey("accepted"))
        addRevenue(priceInCents)
    }

    fun recordDismissed() {
        increment(todayKey("dismissed"))
    }

    fun getTodayShown(): Int = prefs.getInt(todayKey("shown"), 0)

    fun getTodayAccepted(): Int = prefs.getInt(todayKey("accepted"), 0)

    fun getTodayDismissed(): Int = prefs.getInt(todayKey("dismissed"), 0)

    fun getTodayRevenue(): Long = prefs.getLong(todayKey("revenue"), 0L)

    fun getAcceptanceRate(): Float {
        val shown = getTodayShown()
        if (shown == 0) return 0f
        return getTodayAccepted().toFloat() / shown.toFloat() * 100f
    }

    fun getTodayRevenueFormatted(): String {
        val cents = getTodayRevenue()
        return "$${cents / 100}.${"%02d".format(cents % 100)}"
    }

    private fun increment(key: String) {
        prefs.edit().putInt(key, prefs.getInt(key, 0) + 1).apply()
    }

    private fun addRevenue(cents: Long) {
        val key = todayKey("revenue")
        prefs.edit().putLong(key, prefs.getLong(key, 0L) + cents).apply()
    }
}
