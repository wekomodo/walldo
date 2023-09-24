package com.enigmaticdevs.wallhaven.data.billing

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.enigmaticdevs.wallhaven.data.billing.models.Donation
import com.enigmaticdevs.wallhaven.data.billing.models.Entitlement
import com.enigmaticdevs.wallhaven.data.billing.models.WalldoPro

/**
 * No update methods necessary since for each table there is ever expecting one row, hence why
 * the primary key is hardcoded.
 */
@Dao
interface EntitlementsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(walldoPro: WalldoPro)

    @Update
    fun update(walldoPro: WalldoPro)

    @Query("SELECT * FROM walldo_pro LIMIT 1")
    fun getWalldoPro(): LiveData<WalldoPro?>

    @Delete
    fun delete(walldoPro: WalldoPro)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(donationLevel: Donation)

    @Update
    fun update(donationLevel: Donation)

    @Query("SELECT * FROM donation LIMIT 1")
    fun getDonation(): LiveData<Donation?>

    @Delete
    fun delete(donationLevel: Donation)

    @Transaction
    fun insert(vararg entitlements: Entitlement) {
        entitlements.forEach {
            when (it) {
                is Donation -> insert(it)
                is WalldoPro -> insert(it)
            }
        }
    }

    @Transaction
    fun update(vararg entitlements: Entitlement) {
        entitlements.forEach {
            when (it) {
                is Donation -> update(it)
                is WalldoPro -> update(it)
            }
        }
    }

    @Transaction
    fun delete(vararg entitlements: Entitlement) {
        entitlements.forEach {
            when (it) {
                is Donation -> delete(it)
                is WalldoPro -> delete(it)
            }
        }
    }
}