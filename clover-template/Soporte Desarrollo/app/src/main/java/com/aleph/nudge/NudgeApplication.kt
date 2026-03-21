package com.aleph.nudge

import androidx.multidex.MultiDexApplication
import android.util.Log
import com.aleph.nudge.data.StatsManager
import com.aleph.nudge.service.AiService
import com.aleph.nudge.service.DemoDataProvider
import com.aleph.nudge.service.InventoryService

class NudgeApplication : MultiDexApplication() {

    lateinit var aiService: AiService
    lateinit var statsManager: StatsManager
    var inventoryService: InventoryService? = null
        private set

    val isDemoMode: Boolean
        get() = _isDemoMode
    private var _isDemoMode = false

    override fun onCreate() {
        super.onCreate()
        instance = this

        _isDemoMode = !isCloverDevice()
        Log.d("Nudge", "NudgeApplication: isDemoMode=$_isDemoMode")

        statsManager = StatsManager(this)
        aiService = AiService()

        if (_isDemoMode) {
            aiService.setMenuContext(DemoDataProvider.getMenuItems())
        } else {
            inventoryService = InventoryService(this)
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
