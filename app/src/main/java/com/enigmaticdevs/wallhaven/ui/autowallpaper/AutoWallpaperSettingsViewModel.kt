package com.enigmaticdevs.wallhaven.ui.autowallpaper

import androidx.lifecycle.ViewModel
import com.enigmaticdevs.wallhaven.domain.billing.BillingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class AutoWallpaperSettingsViewModel @Inject constructor(private val billingRepository: BillingRepository) : ViewModel() {

    val proLiveData = billingRepository.proLiveData
}