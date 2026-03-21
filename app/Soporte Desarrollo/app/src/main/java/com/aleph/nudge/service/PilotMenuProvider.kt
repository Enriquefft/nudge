package com.aleph.nudge.service

import com.aleph.nudge.model.MenuItem

/**
 * Provides restaurant menu templates for pilot mode.
 *
 * When a non-Clover merchant tests Nudge, they pick one of these templates
 * to populate the menu instead of pulling live inventory from Clover.
 */
object PilotMenuProvider {

    data class MenuTemplate(
        val id: String,
        val name: String,
        val description: String,
        val items: List<MenuItem>
    )

    fun getTemplates(): List<MenuTemplate> = listOf(
        coffeeShopTemplate(),
        burgerPlaceTemplate(),
        pizzaPlaceTemplate(),
        generalStoreTemplate()
    )

    fun getTemplate(id: String): MenuTemplate? = getTemplates().find { it.id == id }

    // =================================================================
    //  Coffee Shop
    // =================================================================

    private fun coffeeShopTemplate(): MenuTemplate = MenuTemplate(
        id = "coffee_shop",
        name = "Coffee Shop",
        description = "Espresso drinks, pastries, and breakfast items",
        items = listOf(
            // Drinks
            MenuItem("PLT_COFF_001", "Espresso", 300L, "Drinks"),
            MenuItem("PLT_COFF_002", "Americano", 350L, "Drinks"),
            MenuItem("PLT_COFF_003", "Latte", 450L, "Drinks"),
            MenuItem("PLT_COFF_004", "Cappuccino", 450L, "Drinks"),
            MenuItem("PLT_COFF_005", "Mocha", 500L, "Drinks"),
            MenuItem("PLT_COFF_006", "Iced Coffee", 400L, "Drinks"),
            MenuItem("PLT_COFF_007", "Matcha Latte", 550L, "Drinks"),
            MenuItem("PLT_COFF_008", "Hot Chocolate", 400L, "Drinks"),
            MenuItem("PLT_COFF_009", "Cold Brew", 450L, "Drinks"),
            MenuItem("PLT_COFF_010", "Chai Latte", 475L, "Drinks"),

            // Food
            MenuItem("PLT_COFF_011", "Croissant", 350L, "Food"),
            MenuItem("PLT_COFF_012", "Blueberry Muffin", 375L, "Food"),
            MenuItem("PLT_COFF_013", "Avocado Toast", 850L, "Food"),
            MenuItem("PLT_COFF_014", "Breakfast Sandwich", 750L, "Food"),
            MenuItem("PLT_COFF_015", "Banana Bread", 325L, "Food"),
            MenuItem("PLT_COFF_016", "Cookie", 250L, "Food"),
            MenuItem("PLT_COFF_017", "Yogurt Parfait", 500L, "Food"),
            MenuItem("PLT_COFF_018", "Bagel", 450L, "Food"),

            // Add-ons
            MenuItem(
                "PLT_COFF_019", "Extra Shot", 75L, "Add-ons",
                isModifier = true, modifierGroupName = "Extras"
            ),
            MenuItem(
                "PLT_COFF_020", "Oat Milk", 60L, "Add-ons",
                isModifier = true, modifierGroupName = "Milk"
            ),
            MenuItem(
                "PLT_COFF_021", "Large Upgrade", 100L, "Add-ons",
                isModifier = true, modifierGroupName = "Size"
            ),
            MenuItem(
                "PLT_COFF_022", "Whipped Cream", 50L, "Add-ons",
                isModifier = true, modifierGroupName = "Extras"
            )
        )
    )

    // =================================================================
    //  Burger Place
    // =================================================================

    private fun burgerPlaceTemplate(): MenuTemplate = MenuTemplate(
        id = "burger_place",
        name = "Burger Place",
        description = "Burgers, sandwiches, fries, and shakes",
        items = listOf(
            // Mains
            MenuItem("PLT_BURG_001", "Classic Burger", 950L, "Mains"),
            MenuItem("PLT_BURG_002", "Cheeseburger", 1050L, "Mains"),
            MenuItem("PLT_BURG_003", "Bacon Burger", 1200L, "Mains"),
            MenuItem("PLT_BURG_004", "Veggie Burger", 1000L, "Mains"),
            MenuItem("PLT_BURG_005", "Chicken Sandwich", 900L, "Mains"),
            MenuItem("PLT_BURG_006", "Fish Sandwich", 1050L, "Mains"),
            MenuItem("PLT_BURG_007", "Hot Dog", 600L, "Mains"),

            // Sides
            MenuItem("PLT_BURG_008", "French Fries", 350L, "Sides"),
            MenuItem("PLT_BURG_009", "Onion Rings", 450L, "Sides"),
            MenuItem("PLT_BURG_010", "Side Salad", 400L, "Sides"),
            MenuItem("PLT_BURG_011", "Coleslaw", 250L, "Sides"),
            MenuItem("PLT_BURG_012", "Sweet Potato Fries", 450L, "Sides"),

            // Drinks
            MenuItem("PLT_BURG_013", "Soda", 250L, "Drinks"),
            MenuItem("PLT_BURG_014", "Lemonade", 300L, "Drinks"),
            MenuItem("PLT_BURG_015", "Milkshake", 550L, "Drinks"),
            MenuItem("PLT_BURG_016", "Iced Tea", 250L, "Drinks"),

            // Add-ons
            MenuItem(
                "PLT_BURG_017", "Extra Patty", 300L, "Add-ons",
                isModifier = true, modifierGroupName = "Extras"
            ),
            MenuItem(
                "PLT_BURG_018", "Add Bacon", 150L, "Add-ons",
                isModifier = true, modifierGroupName = "Extras"
            ),
            MenuItem(
                "PLT_BURG_019", "Add Cheese", 100L, "Add-ons",
                isModifier = true, modifierGroupName = "Extras"
            ),
            MenuItem(
                "PLT_BURG_020", "Large Combo", 250L, "Add-ons",
                isModifier = true, modifierGroupName = "Size"
            )
        )
    )

    // =================================================================
    //  Pizza Place
    // =================================================================

    private fun pizzaPlaceTemplate(): MenuTemplate = MenuTemplate(
        id = "pizza_place",
        name = "Pizza Place",
        description = "Pizzas, wings, salads, and sides",
        items = listOf(
            // Pizzas
            MenuItem("PLT_PIZZA_001", "Margherita", 1200L, "Pizzas"),
            MenuItem("PLT_PIZZA_002", "Pepperoni", 1400L, "Pizzas"),
            MenuItem("PLT_PIZZA_003", "Hawaiian", 1400L, "Pizzas"),
            MenuItem("PLT_PIZZA_004", "Meat Lovers", 1600L, "Pizzas"),
            MenuItem("PLT_PIZZA_005", "Veggie Supreme", 1400L, "Pizzas"),
            MenuItem("PLT_PIZZA_006", "BBQ Chicken", 1500L, "Pizzas"),

            // Sides
            MenuItem("PLT_PIZZA_007", "Garlic Bread", 450L, "Sides"),
            MenuItem("PLT_PIZZA_008", "Caesar Salad", 600L, "Sides"),
            MenuItem("PLT_PIZZA_009", "Wings (6pc)", 800L, "Sides"),
            MenuItem("PLT_PIZZA_010", "Mozzarella Sticks", 650L, "Sides"),
            MenuItem("PLT_PIZZA_011", "Breadsticks", 400L, "Sides"),

            // Drinks
            MenuItem("PLT_PIZZA_012", "Soda", 250L, "Drinks"),
            MenuItem("PLT_PIZZA_013", "Sparkling Water", 300L, "Drinks"),
            MenuItem("PLT_PIZZA_014", "Beer", 500L, "Drinks"),

            // Add-ons
            MenuItem(
                "PLT_PIZZA_015", "Extra Cheese", 200L, "Add-ons",
                isModifier = true, modifierGroupName = "Extras"
            ),
            MenuItem(
                "PLT_PIZZA_016", "Stuffed Crust", 250L, "Add-ons",
                isModifier = true, modifierGroupName = "Crust"
            ),
            MenuItem(
                "PLT_PIZZA_017", "Large Upgrade", 300L, "Add-ons",
                isModifier = true, modifierGroupName = "Size"
            )
        )
    )

    // =================================================================
    //  General Store
    // =================================================================

    private fun generalStoreTemplate(): MenuTemplate = MenuTemplate(
        id = "general_store",
        name = "General Store",
        description = "A mix of snacks, drinks, and everyday basics",
        items = listOf(
            // Snacks
            MenuItem("PLT_GEN_001", "Chips", 250L, "Snacks"),
            MenuItem("PLT_GEN_002", "Granola Bar", 200L, "Snacks"),
            MenuItem("PLT_GEN_003", "Trail Mix", 350L, "Snacks"),
            MenuItem("PLT_GEN_004", "Beef Jerky", 550L, "Snacks"),
            MenuItem("PLT_GEN_005", "Candy Bar", 175L, "Snacks"),
            MenuItem("PLT_GEN_006", "Pretzels", 225L, "Snacks"),

            // Drinks
            MenuItem("PLT_GEN_007", "Bottled Water", 150L, "Drinks"),
            MenuItem("PLT_GEN_008", "Soda Can", 175L, "Drinks"),
            MenuItem("PLT_GEN_009", "Energy Drink", 350L, "Drinks"),
            MenuItem("PLT_GEN_010", "Orange Juice", 300L, "Drinks"),
            MenuItem("PLT_GEN_011", "Iced Tea", 250L, "Drinks"),
            MenuItem("PLT_GEN_012", "Coffee (to-go)", 200L, "Drinks"),

            // Basics
            MenuItem("PLT_GEN_013", "Hand Sanitizer", 400L, "Basics"),
            MenuItem("PLT_GEN_014", "Phone Charger", 1200L, "Basics"),
            MenuItem("PLT_GEN_015", "Sunglasses", 800L, "Basics"),
            MenuItem("PLT_GEN_016", "Umbrella", 1000L, "Basics"),

            // Prepared Food
            MenuItem("PLT_GEN_017", "Sandwich", 650L, "Prepared Food"),
            MenuItem("PLT_GEN_018", "Salad Bowl", 750L, "Prepared Food"),
            MenuItem("PLT_GEN_019", "Soup Cup", 450L, "Prepared Food"),
            MenuItem("PLT_GEN_020", "Fruit Cup", 400L, "Prepared Food")
        )
    )
}
