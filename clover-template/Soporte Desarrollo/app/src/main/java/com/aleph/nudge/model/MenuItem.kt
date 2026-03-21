package com.aleph.nudge.model

data class MenuItem(
    val id: String,
    val name: String,
    val price: Long,
    val category: String?,
    val isModifier: Boolean = false,
    val modifierGroupName: String? = null
) {
    val priceFormatted: String
        get() = "$${price / 100}.${"%02d".format(price % 100)}"
}
