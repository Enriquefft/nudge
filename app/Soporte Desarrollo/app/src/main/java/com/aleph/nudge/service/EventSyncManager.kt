package com.aleph.nudge.service

import android.util.Log
import com.aleph.nudge.data.db.NudgeDatabase
import com.aleph.nudge.data.db.SuggestionEventEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventSyncManager(
    private val database: NudgeDatabase,
    private val backendClient: BackendClient
) {
    private val dao = database.suggestionEventDao()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /** Log an event locally (will be synced later). */
    suspend fun logEvent(
        eventType: String,
        triggerItems: List<String>,
        suggestedItem: String,
        suggestedItemId: String? = null,
        priceCents: Long = 0
    ) {
        withContext(Dispatchers.IO) {
            dao.insert(
                SuggestionEventEntity(
                    eventType = eventType,
                    triggerItems = triggerItems.joinToString(","),
                    suggestedItem = suggestedItem,
                    suggestedItemId = suggestedItemId,
                    priceCents = priceCents
                )
            )
        }
    }

    /** Start periodic sync (every 5 minutes). */
    fun startPeriodicSync() {
        scope.launch {
            while (true) {
                syncPendingEvents()
                delay(5 * 60 * 1000L)
            }
        }
    }

    /** Sync all unsynced events to the backend. */
    suspend fun syncPendingEvents() {
        if (!backendClient.isRegistered) return

        val unsynced = withContext(Dispatchers.IO) { dao.getUnsynced(100) }
        if (unsynced.isEmpty()) return

        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)

        val events = unsynced.map { event ->
            mapOf(
                "event_type" to event.eventType,
                "trigger_items" to event.triggerItems.split(","),
                "suggested_item" to event.suggestedItem,
                "suggested_item_id" to (event.suggestedItemId ?: ""),
                "price_cents" to event.priceCents,
                "timestamp" to isoFormat.format(Date(event.createdAt))
            )
        }

        val success = withContext(Dispatchers.IO) { backendClient.sendEvents(events) }
        if (success) {
            val ids = unsynced.map { it.id }
            withContext(Dispatchers.IO) { dao.markSynced(ids) }
            Log.d("Nudge", "EventSyncManager: synced ${ids.size} events")
        }
    }

    fun cancel() {
        scope.cancel()
    }
}
