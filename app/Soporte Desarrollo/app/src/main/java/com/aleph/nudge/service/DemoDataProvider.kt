package com.aleph.nudge.service

import com.aleph.nudge.model.MenuItem
import com.aleph.nudge.model.Suggestion

/**
 * Provides a realistic coffee-shop menu, five narrative demo scenarios,
 * and fallback suggestions for when the AI backend is unavailable.
 *
 * Each scenario tells a different story about what Nudge can do, designed
 * so the person giving the demo can tap through them in sequence.
 */
object DemoDataProvider {

    // =====================================================================
    //  MENU
    // =====================================================================

    fun getMenuItems(): List<MenuItem> {
        return listOf(
            // ---------- Drinks ----------
            MenuItem("7QXKR2ESP01", "Espresso",       300L, "Drinks"),
            MenuItem("9FMTW3AMR01", "Americano",       350L, "Drinks"),
            MenuItem("4HJNP8LAT01", "Latte",           450L, "Drinks"),
            MenuItem("4HJNP8CAP01", "Cappuccino",      450L, "Drinks"),
            MenuItem("6BVCD1MOC01", "Mocha",           500L, "Drinks"),
            MenuItem("2WLQE5ICE01", "Iced Coffee",     400L, "Drinks"),
            MenuItem("8YKSA6MAT01", "Matcha Latte",    550L, "Drinks"),
            MenuItem("3DNRF7HOT01", "Hot Chocolate",   400L, "Drinks"),
            MenuItem("5TGXM9CLD01", "Cold Brew",       450L, "Drinks"),
            MenuItem("1ZCVB4CHI01", "Chai Latte",      475L, "Drinks"),

            // ---------- Food ----------
            MenuItem("RQWT61CRO01", "Croissant",               350L, "Food"),
            MenuItem("KPLM42MUF01", "Blueberry Muffin",        375L, "Food"),
            MenuItem("YVXN83AVO01", "Avocado Toast",            850L, "Food"),
            MenuItem("JSDF94BRK01", "Breakfast Sandwich",       750L, "Food"),
            MenuItem("HGNB25BAN01", "Banana Bread",             325L, "Food"),
            MenuItem("XWCQ76COO01", "Chocolate Chip Cookie",    250L, "Food"),
            MenuItem("MLPZ17YOG01", "Yogurt Parfait",           500L, "Food"),
            MenuItem("CTKA58BAG01", "Bagel with Cream Cheese",  450L, "Food"),

            // ---------- Modifiers / Add-ons ----------
            MenuItem(
                "MOD_XSHOT01", "Extra Shot", 75L, "Add-ons",
                isModifier = true, modifierGroupName = "Extras"
            ),
            MenuItem(
                "MOD_OTMLK01", "Oat Milk", 60L, "Add-ons",
                isModifier = true, modifierGroupName = "Milk"
            ),
            MenuItem(
                "MOD_LGSZ01", "Large Size Upgrade", 100L, "Add-ons",
                isModifier = true, modifierGroupName = "Size"
            ),
            MenuItem(
                "MOD_WHPCR01", "Whipped Cream", 50L, "Add-ons",
                isModifier = true, modifierGroupName = "Extras"
            ),
            MenuItem(
                "MOD_VNSYR01", "Vanilla Syrup", 50L, "Add-ons",
                isModifier = true, modifierGroupName = "Flavor"
            ),
            MenuItem(
                "MOD_CRDRL01", "Caramel Drizzle", 50L, "Add-ons",
                isModifier = true, modifierGroupName = "Flavor"
            )
        )
    }

    // =====================================================================
    //  DEMO SCENARIOS
    // =====================================================================

    /**
     * Five narrative scenarios, ordered for a live demo walkthrough.
     *
     * Each [DemoScenario] includes:
     * - [DemoScenario.newItemName] – the item the presenter "rings up" next
     * - [DemoScenario.currentOrderItems] – items already in the order
     * - [DemoScenario.fallbackSuggestion] – shown if the AI backend is unavailable
     */
    fun getDemoScenarios(): List<DemoScenario> {
        val menu = menuByName()

        return listOf(
            // Scenario 1 — Simple cross-sell
            DemoScenario(
                newItemName = "Latte",
                currentOrderItems = listOf("Latte"),
                fallbackSuggestion = Suggestion(
                    itemId = menu["Croissant"]!!.id,
                    itemName = "Croissant",
                    price = menu["Croissant"]!!.price,
                    reason = "A fresh croissant pairs perfectly with your latte"
                )
            ),

            // Scenario 2 — Modifier upsell
            DemoScenario(
                newItemName = "Americano",
                currentOrderItems = listOf("Americano"),
                fallbackSuggestion = Suggestion(
                    itemId = menu["Extra Shot"]!!.id,
                    itemName = "Extra Shot",
                    price = menu["Extra Shot"]!!.price,
                    reason = "Go bold \u2014 add an extra shot for just \$0.75"
                )
            ),

            // Scenario 3 — Multi-item intelligence
            DemoScenario(
                newItemName = "Avocado Toast",
                currentOrderItems = listOf("Iced Coffee", "Avocado Toast"),
                fallbackSuggestion = Suggestion(
                    itemId = menu["Large Size Upgrade"]!!.id,
                    itemName = "Large Size Upgrade",
                    price = menu["Large Size Upgrade"]!!.price,
                    reason = "Make it a large for just a dollar more"
                )
            ),

            // Scenario 4 — Learning from history (personalized)
            DemoScenario(
                newItemName = "Cappuccino",
                currentOrderItems = listOf("Cappuccino"),
                fallbackSuggestion = Suggestion(
                    itemId = menu["Oat Milk"]!!.id,
                    itemName = "Oat Milk",
                    price = menu["Oat Milk"]!!.price,
                    reason = "80% of customers add oat milk with their cappuccino"
                )
            ),

            // Scenario 5 — Smart restraint
            DemoScenario(
                newItemName = "Blueberry Muffin",
                currentOrderItems = listOf("Mocha", "Blueberry Muffin"),
                fallbackSuggestion = Suggestion(
                    itemId = menu["Whipped Cream"]!!.id,
                    itemName = "Whipped Cream",
                    price = menu["Whipped Cream"]!!.price,
                    reason = "Top off your mocha with whipped cream"
                )
            )
        )
    }

    // =====================================================================
    //  CUSTOMER CONTEXT (demo personalization)
    // =====================================================================

    /**
     * Returns a customer-context string for scenarios that showcase
     * personalization. Returns null for scenarios without it.
     */
    fun getDemoCustomerContext(scenarioIndex: Int): String? {
        return when (scenarioIndex) {
            3 -> "Customer: Maria (12 previous visits). " +
                 "Frequently orders: Cappuccino (10x), Oat Milk (8x), " +
                 "Croissant (6x), Blueberry Muffin (3x)."
            else -> null
        }
    }

    // =====================================================================
    //  NEXT-ITEM LABEL (for the "Add Item" button in demo mode)
    // =====================================================================

    /**
     * Returns the display name of the item the demo presenter will ring up
     * next for the given scenario index (wraps around).
     */
    fun getNextItemLabel(scenarioIndex: Int): String {
        val scenarios = getDemoScenarios()
        return scenarios[scenarioIndex % scenarios.size].newItemName
    }

    // =====================================================================
    //  DATA CLASS
    // =====================================================================

    data class DemoScenario(
        val newItemName: String,
        val currentOrderItems: List<String>,
        val fallbackSuggestion: Suggestion? = null
    )

    // =====================================================================
    //  INTERNAL HELPERS
    // =====================================================================

    private fun menuByName(): Map<String, MenuItem> {
        return getMenuItems().associateBy { it.name }
    }
}
