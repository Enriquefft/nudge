package com.aleph.nudge.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.concurrent.ConcurrentHashMap

class UpsellHistoryManager(context: Context) {

    companion object {
        private const val TAG = "Nudge"
        private const val PREFS_NAME = "nudge_upsell_history"
        private const val KEY_HISTORY = "pair_history"
        private const val MAX_PAIRS = 200
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    // In-memory cache: "triggerItem\u001FsuggestedItem" -> PairStats
    // Key format changed (lowercase + \u001F delimiter); old SharedPreferences data is not migrated.
    private val history: ConcurrentHashMap<String, PairStats> = loadHistory()

    data class PairStats(
        var accepted: Int = 0,
        var dismissed: Int = 0
    ) {
        val total: Int get() = accepted + dismissed
        val acceptRate: Float get() = if (total == 0) 0f else accepted.toFloat() / total
    }

    fun recordAccepted(triggerItems: List<String>, suggestedItemName: String) {
        for (trigger in triggerItems) {
            val key = pairKey(trigger, suggestedItemName)
            val stats = history.getOrPut(key) { PairStats() }
            stats.accepted++
        }
        persist()
        Log.d(TAG, "UpsellHistory: recorded ACCEPT for '$suggestedItemName' (triggers: $triggerItems)")
    }

    fun recordDismissed(triggerItems: List<String>, suggestedItemName: String) {
        for (trigger in triggerItems) {
            val key = pairKey(trigger, suggestedItemName)
            val stats = history.getOrPut(key) { PairStats() }
            stats.dismissed++
        }
        persist()
        Log.d(TAG, "UpsellHistory: recorded DISMISS for '$suggestedItemName' (triggers: $triggerItems)")
    }

    /**
     * Build a prompt-friendly summary of upsell history relevant to the current order items.
     * Returns null if there's no relevant history.
     */
    fun getHistorySummary(currentItems: List<String>): String? {
        val relevantPairs = mutableListOf<Triple<String, String, PairStats>>()
        val currentItemNames = currentItems.map { it.trim().lowercase() }.toSet()

        for ((key, stats) in history) {
            if (stats.total < 2) continue // need at least 2 data points
            val parts = key.split("\u001F", limit = 2)
            if (parts.size == 2 && parts[0] in currentItemNames) {
                relevantPairs.add(Triple(parts[0], parts[1], stats))
            }
        }

        if (relevantPairs.isEmpty()) return null

        // Sort by total interactions descending
        relevantPairs.sortByDescending { it.third.total }

        val lines = relevantPairs.take(10).map { (trigger, suggested, stats) ->
            val rate = "%.0f".format(stats.acceptRate * 100)
            "- When '$trigger' is ordered, suggesting '$suggested': $rate% accepted (${stats.accepted}/${stats.total})"
        }

        return "Historical upsell performance for items in this order:\n${lines.joinToString("\n")}"
    }

    /** Returns true if demo history has already been seeded. */
    fun hasDemoHistory(): Boolean = history.isNotEmpty()

    /** Pre-seeds realistic upsell pair data for a coffee-shop demo. */
    fun seedDemoHistory() {
        val pairs = listOf(
            // High acceptance
            Triple("latte", "croissant", PairStats(accepted = 12, dismissed = 3)),
            Triple("cappuccino", "oat milk", PairStats(accepted = 8, dismissed = 2)),
            Triple("americano", "extra shot", PairStats(accepted = 6, dismissed = 2)),
            Triple("iced coffee", "large size upgrade", PairStats(accepted = 5, dismissed = 2)),
            Triple("mocha", "whipped cream", PairStats(accepted = 7, dismissed = 2)),
            Triple("matcha latte", "vanilla syrup", PairStats(accepted = 4, dismissed = 1)),
            // Medium acceptance
            Triple("latte", "blueberry muffin", PairStats(accepted = 3, dismissed = 3)),
            Triple("hot chocolate", "croissant", PairStats(accepted = 3, dismissed = 2)),
            // Low acceptance
            Triple("avocado toast", "breakfast sandwich", PairStats(accepted = 1, dismissed = 5)),
            Triple("espresso", "yogurt parfait", PairStats(accepted = 0, dismissed = 4)),
        )

        for ((trigger, suggested, stats) in pairs) {
            history["$trigger\u001F$suggested"] = stats
        }
        persist()
        Log.d(TAG, "UpsellHistory: seeded ${pairs.size} demo pairs")
    }

    private fun pairKey(triggerItem: String, suggestedItem: String): String =
        "${triggerItem.trim().lowercase()}\u001F${suggestedItem.trim().lowercase()}"

    private fun loadHistory(): ConcurrentHashMap<String, PairStats> {
        val json = prefs.getString(KEY_HISTORY, null) ?: return ConcurrentHashMap()
        return try {
            val type = object : TypeToken<MutableMap<String, PairStats>>() {}.type
            val loaded: MutableMap<String, PairStats>? = gson.fromJson(json, type)
            if (loaded != null) ConcurrentHashMap(loaded) else ConcurrentHashMap()
        } catch (e: Exception) {
            Log.w(TAG, "UpsellHistory: failed to load history, starting fresh", e)
            ConcurrentHashMap()
        }
    }

    private fun persist() {
        // Evict least-used pairs if over limit (snapshot-then-retain for ConcurrentHashMap safety)
        if (history.size > MAX_PAIRS) {
            val keysToKeep = history.entries
                .sortedByDescending { it.value.total }
                .take(MAX_PAIRS)
                .map { it.key }
                .toSet()
            history.keys.retainAll(keysToKeep)
        }
        prefs.edit().putString(KEY_HISTORY, gson.toJson(history)).apply()
    }
}
