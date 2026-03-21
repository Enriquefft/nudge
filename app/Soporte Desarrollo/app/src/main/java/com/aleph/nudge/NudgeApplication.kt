package com.aleph.nudge

import android.provider.Settings
import androidx.multidex.MultiDexApplication
import android.util.Log
import io.sentry.android.core.SentryAndroid
import com.aleph.nudge.data.StatsManager
import com.aleph.nudge.data.UpsellHistoryManager
import com.aleph.nudge.data.db.NudgeDatabase
import com.aleph.nudge.service.AiService
import com.aleph.nudge.service.BackendClient
import com.aleph.nudge.service.ConfigManager
import com.aleph.nudge.service.CustomerDataService
import com.aleph.nudge.service.DemoDataProvider
import com.aleph.nudge.service.EventSyncManager
import com.aleph.nudge.service.InventoryService
import com.aleph.nudge.service.PilotMenuProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

enum class AppMode { CLOVER, PILOT, DEMO }

class NudgeApplication : MultiDexApplication() {

    lateinit var aiService: AiService
    lateinit var statsManager: StatsManager
    lateinit var upsellHistoryManager: UpsellHistoryManager
    var backendClient: BackendClient? = null
        private set
    var inventoryService: InventoryService? = null
        private set
    var customerDataService: CustomerDataService? = null
        private set
    var eventSyncManager: EventSyncManager? = null
        private set
    var configManager: ConfigManager? = null
        private set

    /** Gate customer personalization (adds latency, requires merchant to link customers). */
    val customerPersonalizationEnabled: Boolean = false

    var appMode: AppMode = AppMode.PILOT
        private set

    /** Backward-compatible convenience property. */
    val isDemoMode: Boolean get() = appMode == AppMode.DEMO

    /** True when running in pilot mode (non-Clover, non-demo). */
    val isPilotMode: Boolean get() = appMode == AppMode.PILOT

    /** Persisted selected template ID for pilot mode. */
    var selectedTemplateId: String?
        get() = getSharedPreferences("nudge_prefs", MODE_PRIVATE)
            .getString("pilot_template", null)
        set(value) = getSharedPreferences("nudge_prefs", MODE_PRIVATE)
            .edit()
            .putString("pilot_template", value)
            .apply()

    override fun onCreate() {
        super.onCreate()
        instance = this

        if (BuildConfig.SENTRY_DSN.isNotEmpty()) {
            SentryAndroid.init(this) { options ->
                options.dsn = BuildConfig.SENTRY_DSN
                options.isEnableAutoSessionTracking = true
                options.tracesSampleRate = 0.2
                options.environment = if (BuildConfig.IS_DEMO) "demo" else "production"
            }
        }

        appMode = when {
            BuildConfig.IS_DEMO -> AppMode.DEMO
            BuildConfig.IS_CLOVER_BUILD -> AppMode.CLOVER
            else -> AppMode.PILOT
        }
        Log.d("Nudge", "NudgeApplication: appMode=$appMode")
        io.sentry.Sentry.configureScope { scope ->
            scope.setTag("app_mode", appMode.name)
        }

        val database = NudgeDatabase.getInstance(this)
        statsManager = StatsManager(database)
        upsellHistoryManager = UpsellHistoryManager(database)
        aiService = AiService()

        // Initialize backend client and wire it into AiService for proxy support.
        backendClient = BackendClient(this)
        aiService.backendClient = backendClient

        // Background registration: fire-and-forget, app works without it (direct Z.ai fallback).
        if (backendClient?.isRegistered != true) {
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                val merchantName = when (appMode) {
                    AppMode.DEMO -> "Nudge Demo"
                    AppMode.PILOT -> "Pilot Store"
                    AppMode.CLOVER -> "Clover Merchant"
                }
                val registered = backendClient?.register(merchantName, deviceId) ?: false
                Log.d("Nudge", "NudgeApplication: backend registration ${if (registered) "succeeded" else "failed (will use direct fallback)"}")
            }
        }

        // Wire up analytics event sync and remote configuration.
        backendClient?.let { client ->
            val syncManager = EventSyncManager(database, client)
            eventSyncManager = syncManager
            upsellHistoryManager.eventSyncManager = syncManager
            syncManager.startPeriodicSync()

            val cfgManager = ConfigManager(this, client)
            configManager = cfgManager
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                val config = cfgManager.refreshConfig()
                if (config != null) {
                    Log.d("Nudge", "NudgeApplication: remote config loaded: model=${config.aiModel}")
                }
            }
        }

        when (appMode) {
            AppMode.DEMO -> {
                aiService.setMenuContext(DemoDataProvider.getMenuItems())
                seedDemoDataIfNeeded()
            }
            AppMode.PILOT -> {
                // Load saved template menu if one was previously selected
                selectedTemplateId?.let { templateId ->
                    PilotMenuProvider.getTemplate(templateId)?.let { template ->
                        aiService.setMenuContext(template.items)
                        Log.d("Nudge", "NudgeApplication: loaded pilot template '${template.name}'")
                    }
                }
            }
            AppMode.CLOVER -> {
                inventoryService = InventoryService(this)
                if (customerPersonalizationEnabled) {
                    customerDataService = CustomerDataService(this)
                }
            }
        }
    }

    private fun seedDemoDataIfNeeded() {
        if (!statsManager.hasDemoData()) {
            statsManager.seedDemoData()
            Log.d("Nudge", "NudgeApplication: seeded demo stats")
        }
        if (!upsellHistoryManager.hasDemoHistory()) {
            upsellHistoryManager.seedDemoHistory()
            Log.d("Nudge", "NudgeApplication: seeded demo upsell history")
        }
    }

    private fun isCloverDevice(): Boolean {
        return try {
            com.clover.sdk.util.CloverAccount.getAccount(this) != null
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        lateinit var instance: NudgeApplication
            private set
    }
}
