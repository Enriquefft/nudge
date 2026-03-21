package com.aleph.nudge

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.aleph.nudge.data.StatsManager
import com.aleph.nudge.data.UpsellHistoryManager
import com.aleph.nudge.model.MenuItem
import com.aleph.nudge.model.Suggestion
import com.aleph.nudge.service.AiService
import com.aleph.nudge.service.DemoDataProvider
import com.aleph.nudge.service.InventoryService
import com.aleph.nudge.service.OrderObserver
import com.aleph.nudge.service.PilotMenuProvider
import com.aleph.nudge.ui.SuggestionCardView
import com.clover.sdk.util.CloverAccount
import com.clover.sdk.v3.order.Order
import com.clover.sdk.v3.order.OrderConnector
import androidx.core.content.ContextCompat
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
    private var orderSection: View? = null
    private var orderItemsList: LinearLayout? = null
    private var tvOrderTotal: TextView? = null
    private var orderDivider: View? = null
    private var orderTotalRow: View? = null

    // Tracks (name, priceInCents) for display with prices and running total
    private val orderItems = mutableListOf<Pair<String, Long>>()
    private var demoMenuItems: List<MenuItem> = emptyList()

    private var inventoryService: InventoryService? = null
    private lateinit var aiService: AiService
    private lateinit var statsManager: StatsManager
    private lateinit var upsellHistoryManager: UpsellHistoryManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private var orderObserver: OrderObserver? = null
    private var currentOrderId: String? = null
    private var pulseAnimator: ObjectAnimator? = null
    private var suggestionDebounceRunnable: Runnable? = null
    private var isActivityDestroyed = false

    // Demo mode state
    private var scenarioIndex = 0
    private lateinit var scenarios: List<DemoDataProvider.DemoScenario>
    private var btnAddItem: Button? = null
    private var btnRetry: Button? = null

    // Pilot mode state
    private var menuScroll: HorizontalScrollView? = null
    private var menuItemsBar: LinearLayout? = null
    private var pilotMenuItems: List<MenuItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tv_status)
        tvStatsSummary = findViewById(R.id.tv_stats_summary)
        suggestionContainer = findViewById(R.id.suggestion_container)
        btnStats = findViewById(R.id.btn_stats)
        progressLoading = findViewById(R.id.progress_loading)
        orderSection = findViewById(R.id.order_section)
        orderItemsList = findViewById(R.id.order_items_list)
        tvOrderTotal = findViewById(R.id.tv_order_total)
        orderDivider = findViewById(R.id.order_divider)
        orderTotalRow = findViewById(R.id.order_total_row)

        val app = application as NudgeApplication
        inventoryService = app.inventoryService
        aiService = app.aiService
        statsManager = app.statsManager
        upsellHistoryManager = app.upsellHistoryManager
        btnRetry = findViewById(R.id.btn_retry)
        btnRetry?.setOnClickListener {
            btnRetry?.visibility = View.GONE
            loadInventory()
        }

        btnStats.setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        updateStatsSummary()

        when (app.appMode) {
            NudgeApplication.AppMode.DEMO -> setupDemoMode()
            NudgeApplication.AppMode.PILOT -> setupPilotMode()
            NudgeApplication.AppMode.CLOVER -> setupCloverMode()
        }
    }

    // ========== DEMO MODE ==========

    private fun setupDemoMode() {
        scenarios = DemoDataProvider.getDemoScenarios()
        scenarioIndex = 0
        demoMenuItems = DemoDataProvider.getMenuItems()

        btnAddItem = findViewById(R.id.btn_demo_add)
        btnAddItem?.visibility = View.VISIBLE
        btnAddItem?.setOnClickListener { onDemoItemTapped() }
        updateDemoButtonText()

        tvStatus.text = getString(R.string.standalone_mode_status)

        val iconView = findViewById<ImageView>(R.id.iv_icon)
        pulseAnimator = ObjectAnimator.ofFloat(iconView, "alpha", 0.3f, 0.7f)
        pulseAnimator?.duration = 2000
        pulseAnimator?.repeatCount = ObjectAnimator.INFINITE
        pulseAnimator?.repeatMode = ObjectAnimator.REVERSE
        pulseAnimator?.start()

        Log.d(TAG, "MainActivity: demo mode active with ${scenarios.size} scenarios")
    }

    /** Update the demo button to show the next item name */
    private fun updateDemoButtonText() {
        if (scenarios.isEmpty()) return
        val nextScenario = scenarios[scenarioIndex % scenarios.size]
        btnAddItem?.text = getString(R.string.ring_up_item, nextScenario.newItemName)
    }

    private fun onDemoItemTapped() {
        if (scenarios.isEmpty()) return

        val scenario = scenarios[scenarioIndex % scenarios.size]
        val currentScenarioIndex = scenarioIndex % scenarios.size
        scenarioIndex++

        // Build priced item list from scenario
        orderItems.clear()
        for (name in scenario.currentOrderItems) {
            val price = lookupDemoPrice(name)
            orderItems.add(name to price)
        }

        val rootView = findViewById<View>(android.R.id.content)
        Snackbar.make(rootView, getString(R.string.item_added, scenario.newItemName), Snackbar.LENGTH_SHORT)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.brand_primary))
            .setTextColor(ContextCompat.getColor(this, R.color.text_on_brand))
            .show()

        btnAddItem?.isEnabled = false
        tvStatus.text = getString(R.string.ai_analyzing)
        progressLoading.visibility = View.VISIBLE

        val historySummary = upsellHistoryManager.getHistorySummary(scenario.currentOrderItems)
        val customerContext = DemoDataProvider.getDemoCustomerContext(currentScenarioIndex)

        val currentOrderItemNames = scenario.currentOrderItems
        updateOrderItemsDisplay(orderItems)
        updateDemoButtonText()
        aiService.getSuggestion(
            currentOrderItemNames,
            upsellHistory = historySummary,
            customerContext = customerContext
        ) { suggestion ->
            if (isActivityDestroyed) return@getSuggestion
            progressLoading.visibility = View.GONE
            tvStatus.text = getString(R.string.standalone_mode_status)
            val finalSuggestion = suggestion
                ?: scenarios[currentScenarioIndex].fallbackSuggestion
                ?: run {
                    btnAddItem?.isEnabled = true
                    return@getSuggestion
                }
            // Check if suggested item is already in the current order
            val isAlreadyInOrder = currentOrderItemNames.any {
                it.equals(finalSuggestion.itemName, ignoreCase = true)
            }
            if (isAlreadyInOrder) {
                Log.d(TAG, "Skipping suggestion - ${finalSuggestion.itemName} already in order")
                btnAddItem?.isEnabled = true
                return@getSuggestion
            }
            statsManager.recordShown()
            showDemoSuggestionCard(finalSuggestion, currentOrderItemNames)
            updateStatsSummary()
        }
    }

    private fun showDemoSuggestionCard(suggestion: Suggestion, triggerItems: List<String>) {
        suggestionContainer.removeAllViews()

        val card = SuggestionCardView(this)
        card.setOnAddClickListener { onResult ->
            statsManager.recordAccepted(suggestion.price)
            upsellHistoryManager.recordAccepted(triggerItems, suggestion.itemName)
            // Add accepted suggestion to the visible order + total
            orderItems.add(suggestion.itemName to suggestion.price)
            updateOrderItemsDisplay(orderItems)
            Log.d(TAG, "MainActivity: suggestion accepted: ${suggestion.itemName}")
            updateStatsSummary()
            btnAddItem?.isEnabled = true
            onResult(true)
        }
        card.setOnDismissClickListener {
            statsManager.recordDismissed()
            upsellHistoryManager.recordDismissed(triggerItems, suggestion.itemName)
            card.dismiss()
            updateStatsSummary()
            btnAddItem?.isEnabled = true
        }

        suggestionContainer.addView(card)
        card.show(suggestion)
    }

    // ========== PILOT MODE ==========

    private fun setupPilotMode() {
        val app = application as NudgeApplication

        menuScroll = findViewById(R.id.menu_scroll)
        menuItemsBar = findViewById(R.id.menu_items_bar)

        val iconView = findViewById<ImageView>(R.id.iv_icon)
        pulseAnimator = ObjectAnimator.ofFloat(iconView, "alpha", 0.3f, 0.7f)
        pulseAnimator?.duration = 2000
        pulseAnimator?.repeatCount = ObjectAnimator.INFINITE
        pulseAnimator?.repeatMode = ObjectAnimator.REVERSE
        pulseAnimator?.start()

        if (app.selectedTemplateId == null) {
            showTemplatePicker(app)
        } else {
            loadPilotTemplate(app)
        }
    }

    private fun showTemplatePicker(app: NudgeApplication) {
        val templates = PilotMenuProvider.getTemplates()
        val templateNames = templates.map { it.name }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_template))
            .setItems(templateNames) { _, which ->
                app.selectedTemplateId = templates[which].id
                loadPilotTemplate(app)
            }
            .setCancelable(false)
            .show()
    }

    private fun loadPilotTemplate(app: NudgeApplication) {
        val template = PilotMenuProvider.getTemplate(app.selectedTemplateId!!)
        if (template == null) {
            // Template not found, re-pick
            app.selectedTemplateId = null
            showTemplatePicker(app)
            return
        }

        pilotMenuItems = template.items
        aiService.setMenuContext(template.items)

        tvStatus.text = getString(R.string.pilot_mode_ready)
        populatePilotMenuBar(template.items)

        Log.d(TAG, "MainActivity: pilot mode loaded template '${template.name}' with ${template.items.size} items")
    }

    private fun populatePilotMenuBar(items: List<MenuItem>) {
        menuItemsBar?.removeAllViews()

        val nonModifierItems = items.filter { !it.isModifier }
        for (item in nonModifierItems) {
            val pill = Button(this).apply {
                text = "${item.name}  ${item.priceFormatted}"
                textSize = 12f
                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_on_brand))
                isAllCaps = false

                val bg = GradientDrawable().apply {
                    setColor(ContextCompat.getColor(this@MainActivity, R.color.brand_primary))
                    cornerRadius = resources.getDimension(R.dimen.corner_md)
                }
                background = bg

                val hPad = (12 * resources.displayMetrics.density).toInt()
                val vPad = (8 * resources.displayMetrics.density).toInt()
                setPadding(hPad, vPad, hPad, vPad)
                minHeight = 0
                minimumHeight = 0
                minWidth = 0
                minimumWidth = 0

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                val margin = (4 * resources.displayMetrics.density).toInt()
                params.setMargins(margin, 0, margin, 0)
                layoutParams = params

                setOnClickListener { onPilotItemTapped(item) }
            }
            menuItemsBar?.addView(pill)
        }

        menuScroll?.visibility = View.VISIBLE
    }

    private fun onPilotItemTapped(item: MenuItem) {
        orderItems.add(item.name to item.price)
        updateOrderItemsDisplay(orderItems)

        val rootView = findViewById<View>(android.R.id.content)
        Snackbar.make(rootView, getString(R.string.item_added, item.name), Snackbar.LENGTH_SHORT)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.brand_primary))
            .setTextColor(ContextCompat.getColor(this, R.color.text_on_brand))
            .show()

        tvStatus.text = getString(R.string.ai_analyzing)
        progressLoading.visibility = View.VISIBLE
        setMenuBarEnabled(false)

        val currentOrderItemNames = orderItems.map { it.first }
        val historySummary = upsellHistoryManager.getHistorySummary(currentOrderItemNames)

        aiService.getSuggestion(
            currentOrderItemNames,
            upsellHistory = historySummary
        ) { suggestion ->
            if (isActivityDestroyed) return@getSuggestion
            progressLoading.visibility = View.GONE
            tvStatus.text = getString(R.string.pilot_mode_ready)
            setMenuBarEnabled(true)

            if (suggestion == null) {
                Log.d(TAG, "MainActivity: pilot AI returned no suggestion")
                return@getSuggestion
            }

            // Check if suggested item is already in the current order
            val isAlreadyInOrder = currentOrderItemNames.any {
                it.equals(suggestion.itemName, ignoreCase = true)
            }
            if (isAlreadyInOrder) {
                Log.d(TAG, "Skipping suggestion - ${suggestion.itemName} already in order")
                return@getSuggestion
            }

            statsManager.recordShown()
            showPilotSuggestionCard(suggestion, currentOrderItemNames)
            updateStatsSummary()
        }
    }

    private fun setMenuBarEnabled(enabled: Boolean) {
        val bar = menuItemsBar ?: return
        for (i in 0 until bar.childCount) {
            bar.getChildAt(i).isEnabled = enabled
            bar.getChildAt(i).alpha = if (enabled) 1.0f else 0.5f
        }
    }

    private fun showPilotSuggestionCard(suggestion: Suggestion, triggerItems: List<String>) {
        suggestionContainer.removeAllViews()

        val card = SuggestionCardView(this)
        card.setOnAddClickListener { onResult ->
            statsManager.recordAccepted(suggestion.price)
            upsellHistoryManager.recordAccepted(triggerItems, suggestion.itemName)
            orderItems.add(suggestion.itemName to suggestion.price)
            updateOrderItemsDisplay(orderItems)
            Log.d(TAG, "MainActivity: pilot suggestion accepted: ${suggestion.itemName}")
            updateStatsSummary()
            onResult(true)
        }
        card.setOnDismissClickListener {
            statsManager.recordDismissed()
            upsellHistoryManager.recordDismissed(triggerItems, suggestion.itemName)
            card.dismiss()
            updateStatsSummary()
        }

        suggestionContainer.addView(card)
        card.show(suggestion)
    }

    // ========== REAL CLOVER MODE ==========

    private fun setupCloverMode() {
        loadInventory()
    }

    private fun loadInventory() {
        btnRetry?.visibility = View.GONE
        tvStatus.text = getString(R.string.loading_menu)
        inventoryService?.loadInventory { items ->
            if (isActivityDestroyed) return@loadInventory
            if (items.isEmpty()) {
                tvStatus.text = getString(R.string.error_load_menu)
                btnRetry?.visibility = View.VISIBLE
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
                        if (isActivityDestroyed) return@post
                        tvStatus.text = getString(R.string.error_no_account)
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
                    if (isActivityDestroyed) return@post
                    currentOrderId = newOrderId
                    tvStatus.text = getString(R.string.ready_message)
                    startObserving(newOrderId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "MainActivity: failed to create order", e)
                mainHandler.post {
                    if (isActivityDestroyed) return@post
                    tvStatus.text = getString(R.string.error_create_order)
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
        if (isActivityDestroyed) return
        Log.d(TAG, "MainActivity: new item detected, requesting suggestion for ${currentItemNames.size} items")
        // In Clover mode, build priced items from inventory if available, else show names only
        orderItems.clear()
        for (name in currentItemNames) {
            val price = inventoryService?.getMenuItems()
                ?.firstOrNull { it.name.equals(name, ignoreCase = true) }?.price ?: 0L
            orderItems.add(name to price)
        }
        updateOrderItemsDisplay(orderItems)
        tvStatus.text = getString(R.string.ai_analyzing)
        progressLoading.visibility = View.VISIBLE

        // Cancel any pending debounced suggestion request
        suggestionDebounceRunnable?.let { mainHandler.removeCallbacks(it) }

        // Debounce: wait 500ms for more items before requesting
        suggestionDebounceRunnable = Runnable {
            val historySummary = upsellHistoryManager.getHistorySummary(currentItemNames)
            // Fire suggestion immediately - don't wait for customer data
            requestSuggestion(orderId, currentItemNames, historySummary, null)
        }
        mainHandler.postDelayed(suggestionDebounceRunnable!!, 500)
    }

    private fun requestSuggestion(orderId: String, currentItemNames: List<String>, historySummary: String?, customerPrompt: String?) {
        aiService.getSuggestion(currentItemNames, upsellHistory = historySummary, customerContext = customerPrompt) { suggestion ->
            if (isActivityDestroyed) return@getSuggestion
            progressLoading.visibility = View.GONE
            tvStatus.text = getString(R.string.ready_message)
            if (suggestion != null) {
                // Check if suggested item is already in the current order
                val isAlreadyInOrder = currentItemNames.any {
                    it.equals(suggestion.itemName, ignoreCase = true)
                }
                if (isAlreadyInOrder) {
                    Log.d(TAG, "Skipping suggestion - ${suggestion.itemName} already in order")
                    return@getSuggestion
                }
                statsManager.recordShown()
                showSuggestionCard(orderId, suggestion, currentItemNames)
                updateStatsSummary()
            } else {
                Log.d(TAG, "MainActivity: AI returned no suggestion")
            }
        }
    }

    private fun showSuggestionCard(orderId: String, suggestion: Suggestion, triggerItems: List<String>) {
        suggestionContainer.removeAllViews()

        val card = SuggestionCardView(this)
        card.setOnAddClickListener { onResult ->
            orderObserver?.addItemToOrder(orderId, suggestion.itemId) { success ->
                runOnUiThread {
                    if (isActivityDestroyed) return@runOnUiThread
                    if (success) {
                        Log.d(TAG, "MainActivity: added suggestion ${suggestion.itemName} to order")
                        statsManager.recordAccepted(suggestion.price)
                        upsellHistoryManager.recordAccepted(triggerItems, suggestion.itemName)
                        updateStatsSummary()
                    } else {
                        Log.w(TAG, "MainActivity: failed to add suggestion to order")
                    }
                    onResult(success)
                }
            } ?: onResult(false)
        }
        card.setOnDismissClickListener {
            statsManager.recordDismissed()
            upsellHistoryManager.recordDismissed(triggerItems, suggestion.itemName)
            card.dismiss()
            updateStatsSummary()
        }

        suggestionContainer.addView(card)
        card.show(suggestion)
    }

    // ========== SHARED ==========

    private fun updateOrderItemsDisplay(items: List<Pair<String, Long>>) {
        if (items.isEmpty()) {
            orderSection?.visibility = View.GONE
            orderDivider?.visibility = View.GONE
            orderTotalRow?.visibility = View.GONE
            return
        }
        orderSection?.visibility = View.VISIBLE
        orderItemsList?.removeAllViews()

        for ((name, price) in items) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 4, 0, 4)
            }
            val tvName = TextView(this).apply {
                text = name
                textSize = 14f
                setTextColor(resources.getColor(R.color.text_primary, theme))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val tvPrice = TextView(this).apply {
                text = formatCents(price)
                textSize = 14f
                setTextColor(resources.getColor(R.color.text_secondary, theme))
                typeface = android.graphics.Typeface.MONOSPACE
            }
            row.addView(tvName)
            row.addView(tvPrice)
            orderItemsList?.addView(row)
        }

        // Show divider and total
        val totalCents = items.sumOf { it.second }
        orderDivider?.visibility = View.VISIBLE
        orderTotalRow?.visibility = View.VISIBLE
        tvOrderTotal?.text = formatCents(totalCents)
    }

    /** Look up a demo item price by name; returns 0 if not found */
    private fun lookupDemoPrice(name: String): Long {
        return demoMenuItems.firstOrNull { it.name.equals(name, ignoreCase = true) }?.price ?: 0L
    }

    /** Format cents (Long) to "$X.XX" */
    private fun formatCents(cents: Long): String {
        return "$${cents / 100}.${"%02d".format(cents % 100)}"
    }

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
        isActivityDestroyed = true
        super.onDestroy()
        pulseAnimator?.cancel()
        pulseAnimator = null
        suggestionDebounceRunnable?.let { mainHandler.removeCallbacks(it) }
        suggestionDebounceRunnable = null
        orderObserver?.stopObserving()
        orderObserver = null
        Log.d(TAG, "MainActivity: destroyed")
    }
}
