package com.enigmaticdevs.wallhaven.data.billing

 object Sku {
    const val WALLDO_PRO = "pro"
    const val COFFEE = "level_0"
    const val SMOOTHIE = "level_2"
    const val PIZZA = "level_3"
    const val FANCY_MEAL = "level_4"

    val INAPP_PRODUCTS = listOf(WALLDO_PRO, COFFEE, SMOOTHIE, PIZZA, FANCY_MEAL)
    val CONSUMABLE_PRODUCTS = listOf(COFFEE, SMOOTHIE, PIZZA, FANCY_MEAL)
}
