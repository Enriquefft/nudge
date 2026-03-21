package com.aleph.nudge

import androidx.multidex.MultiDexApplication
import android.util.Log
import com.aleph.nudge.data.StatsManager
import com.aleph.nudge.data.UpsellHistoryManager
import com.aleph.nudge.service.AiService
import com.aleph.nudge.service.CustomerDataService
import com.aleph.nudge.service.DemoDataProvider
import com.aleph.nudge.service.InventoryService
import com.aleph.nudge.service.PilotMenuProvider

enum class AppMode { CLOVER, PILOT, DEMO }

class NudgeApplication : MultiDexApplication() {

    lateinit var aiService: AiService
    lateinit var statsManager: StatsManager
    lateinit var upsellHistoryManager: UpsellHistoryManager
    var inventoryService: InventoryService? = null
        private set
    var customerDataService: CustomerDataService? = null
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

        appMode = when {
            BuildConfig.IS_DEMO -> AppMode.DEMO
            BuildConfig.IS_CLOVER_BUILD -> AppMode.CLOVER
            else -> AppMode.PILOT
        }
        Log.d("Nudge", "NudgeApplication: appMode=$appMode")

        statsManager = StatsManager(this)
        upsellHistoryManager = UpsellHistoryManager(this)
        aiService = AiService()

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
