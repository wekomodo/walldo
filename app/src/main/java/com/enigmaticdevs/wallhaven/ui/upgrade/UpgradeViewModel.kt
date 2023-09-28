package com.enigmaticdevs.wallhaven.ui.upgrade

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.enigmaticdevs.wallhaven.data.billing.Sku
import com.enigmaticdevs.wallhaven.domain.billing.BillingRepository
import com.enigmaticdevs.wallhaven.util.livedata.combineWith
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UpgradeViewModel @Inject constructor(private val billingRepository: BillingRepository) : ViewModel(){

    private val productDetailsLiveData = billingRepository.productsWithProductDetails
    val proLiveData = billingRepository.proLiveData


    val canPurchaseLiveData = productDetailsLiveData.combineWith(proLiveData).map {
        val proProductDetails = it.first?.get(Sku.WALLDO_PRO)
        val entitled = it.second?.entitled
        return@map proProductDetails != null && entitled != true
    }

    val billingMessageLiveData = billingRepository.billingMessageLiveData

    val billingErrorLiveData = billingRepository.billingErrorLiveData


    fun makePurchase(activity: Activity) {
        productDetailsLiveData.value?.get(Sku.WALLDO_PRO)?.let {
            billingRepository.launchBillingFlow(activity, it)
        }
    }

    fun restorePurchase() = billingRepository.queryPurchasesAsync(restore = true)

    companion object {

        private const val WALLDO_COLLECTION_ID = "12334758"
    }
}