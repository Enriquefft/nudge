package com.aleph.nudge.model

data class Suggestion(
    val itemId: String,
    val itemName: String,
    val price: Long,
    val reason: String
) {
    val priceFormatted: String
        get() = "$${price / 100}.${"%02d".format(price % 100)}"
}
