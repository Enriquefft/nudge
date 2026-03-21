package com.aleph.nudge

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.aleph.nudge.data.StatsManager
import com.aleph.nudge.model.Suggestion
import com.aleph.nudge.service.AiService
import com.aleph.nudge.service.DemoDataProvider
import com.aleph.nudge.service.InventoryService
import com.aleph.nudge.service.OrderObserver
import com.aleph.nudge.ui.SuggestionCardView
import com.clover.sdk.util.CloverAccount
import com.clover.sdk.v3.order.Order
import com.clover.sdk.v3.order.OrderConnector
import com.google.android.material.snackbar.Snackbar

@Suppress("DEPRECATION")
class MainActivity : android.app.Activity() {

    companion object {
        private const val TAG = "Nudge"
    }

    private lateinit var tvStatus: TextView
    private lateinit var tvStatsSummary: TextView
    private lateinit var suggestionContainer: FrameLayout
    private lateinit var btnStats: ImageButton
    private lateinit var progressLoading: ProgressBar

    private var inventoryService: InventoryService? = null
    private lateinit var aiService: AiService
    private lateinit var statsManager: StatsManager

    private val mainHandler = Handler(Looper.getMainLooper())
    private var orderObserver: OrderObserver? = null
    private var currentOrderId: String? = null
    private var pulseAnimator: ObjectAnimator? = null

    // Standalone mode state (non-Clover device)
    private var isStandaloneMode = false
    private var scenarioIndex = 0
    private lateinit var scenarios: List<DemoDataProvider.DemoScenario>
    private var btnAddItem: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tv_status)
        tvStatsSummary = findViewById(R.id.tv_stats_summary)
        suggestionContainer = findViewById(R.id.suggestion_container)
        btnStats = findViewById(R.id.btn_stats)
        progressLoading = findViewById(R.id.progress_loading)

        val app = application as NudgeApplication
        isStandaloneMode = app.isDemoMode
        inventoryService = app.inventoryService
        aiService = app.aiService
        statsManager = app.statsManager

        btnStats.setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        updateStatsSummary()

        if (isStandaloneMode) {
            setupStandaloneMode()
        } else {
            loadInventory()
        }
    }

    // ========== STANDALONE MODE ==========

    private fun setupStandaloneMode() {
        scenarios = DemoDataProvider.getDemoScenarios()
        scenarioIndex = 0

        btnAddItem = findViewById(R.id.btn_demo_add)
        btnAddItem?.visibility = View.VISIBLE
        btnAddItem?.setOnClickListener { onAddItemTapped() }

        tvStatus.text = getString(R.string.standalone_mode_status)

        val iconView = findViewById<ImageView>(R.id.iv_icon)
        pulseAnimator = ObjectAnimator.ofFloat(iconView, "alpha", 0.3f, 0.7f)
        pulseAnimator?.duration = 2000
        pulseAnimator?.repeatCount = ObjectAnimator.INFINITE
        pulseAnimator?.repeatMode = ObjectAnimator.REVERSE
        pulseAnimator?.start()

        Log.d(TAG, "MainActivity: standalone mode active with ${scenarios.size} scenarios")
    }

    private fun onAddItemTapped() {
        if (scenarios.isEmpty()) return

        val scenario = scenarios[scenarioIndex % scenarios.size]
        scenarioIndex++

        val rootView = findViewById<View>(android.R.id.content)
        Snackbar.make(rootView, getString(R.string.item_added, scenario.newItemName), Snackbar.LENGTH_SHORT)
            .setBackgroundTint(resources.getColor(R.color.brand_primary))
            .setTextColor(resources.getColor(R.color.text_on_brand))
            .show()

        btnAddItem?.isEnabled = false
        progressLoading.visibility = View.VISIBLE

        aiService.getSuggestion(scenario.currentOrderItems) { suggestion ->
            progressLoading.visibility = View.GONE
            val finalSuggestion = suggestion ?: getFallbackSuggestion(scenario.currentOrderItems)
            statsManager.recordShown()
            showStandaloneSuggestionCard(finalSuggestion)
            updateStatsSummary()
        }
    }

    private fun getFallbackSuggestion(currentItems: List<String>): Suggestion {
        val menu = DemoDataProvider.getMenuItems().filter { !it.isModifier }
        val currentLower = currentItems.map { it.lowercase() }

        val hasDrink = currentLower.any { name ->
            menu.any { it.category == "Drinks" && it.name.lowercase() == name }
        }
        val hasSide = currentLower.any { name ->
            menu.any { it.category == "Sides" && it.name.lowercase() == name }
        }

        val candidate = when {
            !hasSide -> menu.first { it.category == "Sides" && it.name.lowercase() !in currentLower }
            !hasDrink -> menu.first { it.category == "Drinks" && it.name.lowercase() !in currentLower }
            else -> menu.first { it.name.lowercase() !in currentLower }
        }

        val reason = when (candidate.category) {
            "Sides" -> "Most people add ${candidate.name.lowercase()} with this"
            "Drinks" -> "A ${candidate.name.lowercase()} goes great with that"
            "Desserts" -> "Save room for a ${candidate.name.lowercase()}"
            else -> "You might also enjoy ${candidate.name.lowercase()}"
        }

        return Suggestion(
            itemId = candidate.id,
            itemName = candidate.name,
            price = candidate.price,
            reason = reason
        )
    }

    private fun showStandaloneSuggestionCard(suggestion: Suggestion) {
        suggestionContainer.removeAllViews()

        val card = SuggestionCardView(this)
        card.setOnAddClickListener {
            statsManager.recordAccepted(suggestion.price)
            Log.d(TAG, "MainActivity: suggestion accepted: ${suggestion.itemName}")
            updateStatsSummary()
            btnAddItem?.isEnabled = true
        }
        card.setOnDismissClickListener {
            statsManager.recordDismissed()
            card.dismiss()
            updateStatsSummary()
            btnAddItem?.isEnabled = true
        }

        suggestionContainer.addView(card)
        card.show(suggestion)
    }

    // ========== REAL CLOVER MODE ==========

    private fun loadInventory() {
        tvStatus.text = getString(R.string.loading_menu)
        inventoryService?.loadInventory { items ->
            if (items.isEmpty()) {
                tvStatus.text = "Failed to load menu. Check connection."
                Log.w(TAG, "MainActivity: inventory loaded 0 items")
                return@loadInventory
            }
            aiService.setMenuContext(items)
            tvStatus.text = getString(R.string.ready_message)
            Log.d(TAG, "MainActivity: menu loaded with ${items.size} items")
            connectToOrder()
        }
    }

    private fun connectToOrder() {
        val orderId = intent.getStringExtra("clover.intent.extra.ORDER_ID")
        if (orderId != null) {
            Log.d(TAG, "MainActivity: received order ID from intent: $orderId")
            currentOrderId = orderId
            startObserving(orderId)
        } else {
            Log.d(TAG, "MainActivity: no order ID in intent, creating new order")
            tvStatus.text = getString(R.string.waiting_for_order)
            createNewOrder()
        }
    }

    private fun createNewOrder() {
        Thread {
            var connector: OrderConnector? = null
            try {
                val account = CloverAccount.getAccount(this)
                if (account == null) {
                    mainHandler.post {
                        tvStatus.text = "No Clover account found."
                        Log.w(TAG, "MainActivity: CloverAccount is null")
                    }
                    return@Thread
                }
                connector = OrderConnector(this, account, null)
                connector.connect()
                val order = connector.createOrder(Order())
                val newOrderId = order.id
                Log.d(TAG, "MainActivity: created new order $newOrderId")
                mainHandler.post {
                    currentOrderId = newOrderId
                    tvStatus.text = getString(R.string.ready_message)
                    startObserving(newOrderId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "MainActivity: failed to create order", e)
                mainHandler.post {
                    tvStatus.text = "Could not create order. Retry by reopening."
                }
            } finally {
                try {
                    connector?.disconnect()
                } catch (e: Exception) {
                    Log.w(TAG, "MainActivity: error disconnecting order connector", e)
                }
            }
        }.start()
    }

    private fun startObserving(orderId: String) {
        orderObserver = OrderObserver(this) { oid, itemNames ->
            onNewItemAdded(oid, itemNames)
        }
        orderObserver?.startObserving(orderId)
        Log.d(TAG, "MainActivity: observing order $orderId")
    }

    private fun onNewItemAdded(orderId: String, currentItemNames: List<String>) {
        Log.d(TAG, "MainActivity: new item detected, requesting suggestion for ${currentItemNames.size} items")
        progressLoading.visibility = View.VISIBLE
        aiService.getSuggestion(currentItemNames) { suggestion ->
            progressLoading.visibility = View.GONE
            if (suggestion != null) {
                statsManager.recordShown()
                showSuggestionCard(orderId, suggestion)
                updateStatsSummary()
            } else {
                Log.d(TAG, "MainActivity: AI returned no suggestion")
            }
        }
    }

    private fun showSuggestionCard(orderId: String, suggestion: Suggestion) {
        suggestionContainer.removeAllViews()

        val card = SuggestionCardView(this)
        card.setOnAddClickListener {
            orderObserver?.addItemToOrder(orderId, suggestion.itemId) { success ->
                if (success) {
                    Log.d(TAG, "MainActivity: added suggestion ${suggestion.itemName} to order")
                } else {
                    Log.w(TAG, "MainActivity: failed to add suggestion to order")
                }
            }
            statsManager.recordAccepted(suggestion.price)
            updateStatsSummary()
        }
        card.setOnDismissClickListener {
            statsManager.recordDismissed()
            card.dismiss()
            updateStatsSummary()
        }

        suggestionContainer.addView(card)
        card.show(suggestion)
    }

    // ========== SHARED ==========

    private fun updateStatsSummary() {
        val accepted = statsManager.getTodayAccepted()
        if (accepted > 0) {
            val revenue = statsManager.getTodayRevenueFormatted()
            tvStatsSummary.text = "$accepted suggestion${if (accepted != 1) "s" else ""} accepted today (+$revenue)"
            tvStatsSummary.visibility = View.VISIBLE
        } else {
            tvStatsSummary.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pulseAnimator?.cancel()
        pulseAnimator = null
        orderObserver?.stopObserving()
        orderObserver = null
        Log.d(TAG, "MainActivity: destroyed")
    }
}
