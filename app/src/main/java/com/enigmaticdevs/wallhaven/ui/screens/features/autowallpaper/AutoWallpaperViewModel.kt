package com.enigmaticdevs.wallhaven.ui.screens.features.autowallpaper

import androidx.lifecycle.ViewModel
import com.enigmaticdevs.wallhaven.data.repository.AutoWallpaperInterval
import com.enigmaticdevs.wallhaven.data.repository.AutoWallpaperRepository
import com.enigmaticdevs.wallhaven.data.repository.AutoWallpaperState
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.StateFlow


@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class AutoWallpaperViewModel(
    private val repository: AutoWallpaperRepository

) : ViewModel() {

    val state : StateFlow<AutoWallpaperState> = repository.state

    fun updateSwitch(value : Boolean) = repository.updateSwitch(value)
    fun updateOnWifi(value: Boolean) = repository.updateOnWifi(value)
    fun updateOnCharging(value: Boolean) = repository.updateOnCharging(value)
    fun updatePauseOnChargingLow(value: Boolean) = repository.updatePauseOnBatteryLow(value)
    fun updateOnIdle(value: Boolean) = repository.updateOnIdle(value)
    fun updateInterval(value : String) = repository.updateInterval(value)
    fun updateIfLowOnStorage(value: Boolean) = repository.updateIfLowOnStorage(value)

    fun updateOrientationFix(value: Boolean) = repository.updateOrientationFix(value)

    fun updateScreenSelect(value : String) = repository.updateScreenSelect(value)
}