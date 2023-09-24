package com.enigmaticdevs.wallhaven.data.billing.models

import androidx.room.Entity
import androidx.room.PrimaryKey

const val LEVEL_COFFEE = 1
const val LEVEL_SMOOTHIE = 5
const val LEVEL_PIZZA = 10
const val LEVEL_FANCY_MEAL = 15

abstract class Entitlement {
    @PrimaryKey
    var id: Int = 1

    abstract fun mayPurchase(): Boolean
}

@Entity(tableName = "walldo_pro")
data class WalldoPro(val entitled: Boolean) : Entitlement() {
    override fun mayPurchase(): Boolean = !entitled
}

@Entity(tableName = "donation")
class Donation(val level: Int) : Entitlement() {

    override fun mayPurchase(): Boolean = true
}
