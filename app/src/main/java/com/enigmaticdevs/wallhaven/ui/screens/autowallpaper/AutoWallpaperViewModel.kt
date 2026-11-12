package com.enigmaticdevs.wallhaven.ui.screens.autowallpaper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionsDatabase
import com.enigmaticdevs.wallhaven.data.remote.model.RemoteCollections
import com.enigmaticdevs.wallhaven.data.repository.AutoWallpaperInterval
import com.enigmaticdevs.wallhaven.data.repository.AutoWallpaperRepository
import com.enigmaticdevs.wallhaven.data.repository.AutoWallpaperState
import com.enigmaticdevs.wallhaven.data.repository.CollectionsRepository
import com.enigmaticdevs.wallhaven.data.repository.GlobalFilterRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class AutoWallpaperViewModel(
    private val repository: AutoWallpaperRepository,
    private val globalFilterRepository: GlobalFilterRepository,
    private val collectionsRepository: CollectionsRepository,

) : ViewModel() {

    val state : StateFlow<AutoWallpaperState> = repository.state


    @OptIn(ExperimentalCoroutinesApi::class)
    val collectionsInfo = globalFilterRepository.filters.map {
        it.purity
    }.distinctUntilChanged().flatMapLatest { purity ->
        collectionsRepository.getCollectionInfo(purity = purity)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateSwitch(value : Boolean) = repository.updateSwitch(value)
    fun updateOnWifi(value: Boolean) = repository.updateOnWifi(value)
    fun updateOnCharging(value: Boolean) = repository.updateOnCharging(value)
    fun updatePauseOnChargingLow(value: Boolean) = repository.updatePauseOnBatteryLow(value)
    fun updateOnIdle(value: Boolean) = repository.updateOnIdle(value)
    fun updateInterval(value : String) = repository.updateInterval(value)
    fun updateIfLowOnStorage(value: Boolean) = repository.updateIfLowOnStorage(value)

    fun updateOrientationFix(value: Boolean) = repository.updateOrientationFix(value)

    fun updateScreenSelect(value : String) = repository.updateScreenSelect(value)

    fun updateSource(value : String) = repository.updateSource(value)

    fun updateAutoWallpaperCollection(value : Long) {
        viewModelScope.launch {
            collectionsRepository.updateAutoWallpaperCollection(value)
        }
    }
}