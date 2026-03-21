package com.aleph.nudge.service

import com.aleph.nudge.model.MenuItem

object DemoDataProvider {

    fun getMenuItems(): List<MenuItem> {
        return listOf(
            // Mains
            MenuItem("m1", "Classic Burger", 1299, "Mains"),
            MenuItem("m2", "Grilled Chicken Sandwich", 1199, "Mains"),
            MenuItem("m3", "Fish Tacos", 1399, "Mains"),
            MenuItem("m4", "Caesar Salad", 999, "Mains"),
            MenuItem("m5", "Margherita Pizza", 1499, "Mains"),

            // Sides
            MenuItem("s1", "French Fries", 499, "Sides"),
            MenuItem("s2", "Onion Rings", 599, "Sides"),
            MenuItem("s3", "Sweet Potato Fries", 599, "Sides"),
            MenuItem("s4", "Coleslaw", 399, "Sides"),
            MenuItem("s5", "Side Salad", 499, "Sides"),

            // Drinks
            MenuItem("d1", "Craft Lemonade", 449, "Drinks"),
            MenuItem("d2", "Iced Tea", 349, "Drinks"),
            MenuItem("d3", "Milkshake", 699, "Drinks"),
            MenuItem("d4", "Soda", 299, "Drinks"),
            MenuItem("d5", "Sparkling Water", 249, "Drinks"),

            // Desserts
            MenuItem("x1", "Brownie Sundae", 799, "Desserts"),
            MenuItem("x2", "Cheesecake Slice", 699, "Desserts"),
            MenuItem("x3", "Apple Pie", 599, "Desserts"),

            // Appetizers
            MenuItem("a1", "Mozzarella Sticks", 799, "Appetizers"),
            MenuItem("a2", "Wings (8pc)", 1099, "Appetizers"),
            MenuItem("a3", "Loaded Nachos", 999, "Appetizers"),

            // Modifiers/Add-ons
            MenuItem("mod1", "Add Bacon", 199, "Add-ons", isModifier = true, modifierGroupName = "Extras"),
            MenuItem("mod2", "Add Avocado", 179, "Add-ons", isModifier = true, modifierGroupName = "Extras"),
            MenuItem("mod3", "Extra Cheese", 129, "Add-ons", isModifier = true, modifierGroupName = "Extras"),
            MenuItem("mod4", "Upgrade to Large", 200, "Add-ons", isModifier = true, modifierGroupName = "Size")
        )
    }

    fun getDemoScenarios(): List<DemoScenario> {
        return listOf(
            DemoScenario("Classic Burger", listOf("Classic Burger")),
            DemoScenario("Fish Tacos", listOf("Classic Burger", "Fish Tacos")),
            DemoScenario("Margherita Pizza", listOf("Margherita Pizza")),
            DemoScenario("Grilled Chicken Sandwich", listOf("Grilled Chicken Sandwich", "Iced Tea")),
            DemoScenario("Wings (8pc)", listOf("Wings (8pc)")),
            DemoScenario("Caesar Salad", listOf("Caesar Salad"))
        )
    }

    data class DemoScenario(
        val newItemName: String,
        val currentOrderItems: List<String>
    )
}
