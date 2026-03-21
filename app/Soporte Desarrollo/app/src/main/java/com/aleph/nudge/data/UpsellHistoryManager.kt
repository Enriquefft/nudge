package com.aleph.nudge.data

import android.util.Log
import com.aleph.nudge.data.db.NudgeDatabase
import com.aleph.nudge.data.db.UpsellPairEntity
import com.aleph.nudge.service.EventSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class UpsellHistoryManager(private val db: NudgeDatabase) {

    companion object {
        private const val TAG = "Nudge"
        private const val MAX_PAIRS = 200
    }

    private val upsellPairDao = db.upsellPairDao()

    /** Set by NudgeApplication to enable analytics event logging. */
    var eventSyncManager: EventSyncManager? = null

    suspend fun recordAccepted(triggerItems: List<String>, suggestedItemName: String) = withContext(Dispatchers.IO) {
        for (trigger in triggerItems) {
            val key = pairKey(trigger, suggestedItemName)
            val existing = upsellPairDao.getByKey(key)
            if (existing != null) {
                upsellPairDao.upsert(
                    existing.copy(
                        accepted = existing.accepted + 1,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            } else {
                upsellPairDao.upsert(
                    UpsellPairEntity(
                        pairKey = key,
                        triggerItem = trigger.trim().lowercase(),
                        suggestedItem = suggestedItemName.trim().lowercase(),
                        accepted = 1,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            }
        }
        evictIfNeeded()
        Log.d(TAG, "UpsellHistory: recorded ACCEPT for '$suggestedItemName' (triggers: $triggerItems)")

        // Fire-and-forget analytics event (don't block the accept action)
        eventSyncManager?.let { manager ->
            CoroutineScope(Dispatchers.IO).launch {
                manager.logEvent("accepted", triggerItems, suggestedItemName)
            }
        }
    }

    suspend fun recordDismissed(triggerItems: List<String>, suggestedItemName: String) = withContext(Dispatchers.IO) {
        for (trigger in triggerItems) {
            val key = pairKey(trigger, suggestedItemName)
            val existing = upsellPairDao.getByKey(key)
            if (existing != null) {
                upsellPairDao.upsert(
                    existing.copy(
                        dismissed = existing.dismissed + 1,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            } else {
                upsellPairDao.upsert(
                    UpsellPairEntity(
                        pairKey = key,
                        triggerItem = trigger.trim().lowercase(),
                        suggestedItem = suggestedItemName.trim().lowercase(),
                        dismissed = 1,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            }
        }
        evictIfNeeded()
        Log.d(TAG, "UpsellHistory: recorded DISMISS for '$suggestedItemName' (triggers: $triggerItems)")

        // Fire-and-forget analytics event (don't block the dismiss action)
        eventSyncManager?.let { manager ->
            CoroutineScope(Dispatchers.IO).launch {
                manager.logEvent("dismissed", triggerItems, suggestedItemName)
            }
        }
    }

    /**
     * Build a prompt-friendly summary of upsell history relevant to the current order items.
     * Returns null if there's no relevant history.
     */
    suspend fun getHistorySummary(currentItems: List<String>): String? = withContext(Dispatchers.IO) {
        val currentItemNames = currentItems.map { it.trim().lowercase() }.toSet()
        val allPairs = upsellPairDao.getAll()

        val relevantPairs = allPairs.filter { pair ->
            val total = pair.accepted + pair.dismissed
            total >= 2 && pair.triggerItem in currentItemNames
        }

        if (relevantPairs.isEmpty()) return@withContext null

        val sorted = relevantPairs.sortedByDescending { it.accepted + it.dismissed }

        val lines = sorted.take(10).map { pair ->
            val total = pair.accepted + pair.dismissed
            val rate = if (total == 0) 0f else pair.accepted.toFloat() / total * 100f
            "- When '${pair.triggerItem}' is ordered, suggesting '${pair.suggestedItem}': ${"%.0f".format(rate)}% accepted (${pair.accepted}/$total)"
        }

        "Historical upsell performance for items in this order:\n${lines.joinToString("\n")}"
    }

    /**
     * Returns true if demo history has already been seeded.
     * Uses runBlocking because it is called from Application.onCreate().
     */
    fun hasDemoHistory(): Boolean = runBlocking(Dispatchers.IO) {
        upsellPairDao.count() > 0
    }

    /**
     * Pre-seeds realistic upsell pair data for a coffee-shop demo.
     * Uses runBlocking because it is called from Application.onCreate().
     */
    fun seedDemoHistory() = runBlocking(Dispatchers.IO) {
        val pairs = listOf(
            // High acceptance
            Triple("latte", "croissant", PairData(accepted = 12, dismissed = 3)),
            Triple("cappuccino", "oat milk", PairData(accepted = 8, dismissed = 2)),
            Triple("americano", "extra shot", PairData(accepted = 6, dismissed = 2)),
            Triple("iced coffee", "large size upgrade", PairData(accepted = 5, dismissed = 2)),
            Triple("mocha", "whipped cream", PairData(accepted = 7, dismissed = 2)),
            Triple("matcha latte", "vanilla syrup", PairData(accepted = 4, dismissed = 1)),
            // Medium acceptance
            Triple("latte", "blueberry muffin", PairData(accepted = 3, dismissed = 3)),
            Triple("hot chocolate", "croissant", PairData(accepted = 3, dismissed = 2)),
            // Low acceptance
            Triple("avocado toast", "breakfast sandwich", PairData(accepted = 1, dismissed = 5)),
            Triple("espresso", "yogurt parfait", PairData(accepted = 0, dismissed = 4)),
        )

        for ((trigger, suggested, data) in pairs) {
            val key = "$trigger\u001F$suggested"
            upsellPairDao.upsert(
                UpsellPairEntity(
                    pairKey = key,
                    triggerItem = trigger,
                    suggestedItem = suggested,
                    accepted = data.accepted,
                    dismissed = data.dismissed,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }
        Log.d(TAG, "UpsellHistory: seeded ${pairs.size} demo pairs")
    }

    private fun pairKey(triggerItem: String, suggestedItem: String): String =
        "${triggerItem.trim().lowercase()}\u001F${suggestedItem.trim().lowercase()}"

    private fun evictIfNeeded() {
        val count = upsellPairDao.count()
        if (count > MAX_PAIRS) {
            upsellPairDao.deleteOldest(count - MAX_PAIRS)
        }
    }

    /** Simple holder for demo seed data. */
    private data class PairData(val accepted: Int, val dismissed: Int)
}
