package com.enigmaticdevs.wallhaven.ui.donation

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.ProductDetails
import com.enigmaticdevs.wallhaven.domain.billing.BillingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class DonationViewModel @Inject constructor (
    private val billingRepository: BillingRepository
) : ViewModel()  {

    val productDetailsLiveData = billingRepository.productsWithProductDetails

    val purchaseCompleteLiveData = billingRepository.purchaseCompleteLiveData

    fun makePurchase(activity: Activity, productDetails: ProductDetails) {
        billingRepository.launchBillingFlow(activity, productDetails)
    }

    override fun onCleared() {
        super.onCleared()
        billingRepository.endDataSourceConnections()
    }
}