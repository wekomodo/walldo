package com.enigmaticdevs.wallhaven.ui.screens.autowallpaper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.enigmaticdevs.wallhaven.data.repository.AutoWallpaperInterval
import com.enigmaticdevs.wallhaven.data.repository.AutoWallpaperRepository
import com.enigmaticdevs.wallhaven.data.repository.AutoWallpaperState
import com.enigmaticdevs.wallhaven.data.repository.CollectionsRepository
import com.enigmaticdevs.wallhaven.data.repository.GlobalFilterRepository
import com.enigmaticdevs.wallhaven.data.repository.GlobalFlagsRepository
import com.enigmaticdevs.wallhaven.work.AutoWallpaperWorker
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class AutoWallpaperViewModel(
    private val repository: AutoWallpaperRepository,
    private val globalFilterRepository: GlobalFilterRepository,
    private val collectionsRepository: CollectionsRepository,
    private val workManager: WorkManager,
    private val globalFlagsRepository: GlobalFlagsRepository

) : ViewModel() {

    val state: StateFlow<AutoWallpaperState> = repository.state
    private val _events = Channel<String>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        state
            .map { s ->
                // Zero out non-scheduling fields so changes to them don't re-trigger WorkManager
                s.copy(
                    source = "",
                    orientationFix = false,
                    screenSelect = "",
                    lastWallpaperId = null,
                    popularLastPage = 0
                )
            }
            .distinctUntilChanged()
            .drop(1) // Skip initial startup emission so WorkManager isn't re-enqueued on every cold launch
            .onEach { s ->
                scheduleAutoWallpaper(s)
            }
            .launchIn(viewModelScope)
    }
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

    fun updateSwitch(value: Boolean) = repository.updateSwitch(value)

    fun updateOnWifi(value: Boolean) = repository.updateOnWifi(value)
    fun updateOnCharging(value: Boolean) = repository.updateOnCharging(value)
    fun updatePauseOnChargingLow(value: Boolean) = repository.updatePauseOnBatteryLow(value)
    fun updateOnIdle(value: Boolean) = repository.updateOnIdle(value)
    fun updateInterval(value: String) = repository.updateInterval(value)
    fun updateIfLowOnStorage(value: Boolean) = repository.updateIfLowOnStorage(value)

    fun updateOrientationFix(value: Boolean) = repository.updateOrientationFix(value)

    fun updateScreenSelect(value: String) = repository.updateScreenSelect(value)

    fun updateSource(value: String) = repository.updateSource(value)

    fun updateAutoWallpaperCollection(value: Long) {
        viewModelScope.launch {
            if (globalFlagsRepository.isPro.first()) {
            collectionsRepository.updateAutoWallpaperCollection(value)
                repository.updateLastWallpaperId(null)
            } else
                _events.send("Pro")
        }
    }


    fun scheduleAutoWallpaper(
        s: AutoWallpaperState
    ) {
        if (!s.switch) {
            workManager.cancelUniqueWork("auto_wallpaper")
            return
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(
                if (s.onWifi) NetworkType.UNMETERED else NetworkType.CONNECTED
            )
            .setRequiresCharging(s.onCharging)
            .setRequiresDeviceIdle(s.onIdle)
            .setRequiresBatteryNotLow(s.pauseOnLowBattery)
            .setRequiresStorageNotLow(s.ifLowOnStorage)
            .build()
        val interval = AutoWallpaperInterval.fromName(s.interval)
        val request = PeriodicWorkRequestBuilder<AutoWallpaperWorker>(
            repeatInterval = maxOf(interval.value, 15L),
            repeatIntervalTimeUnit = interval.timeUnit
        )
            .setConstraints(constraints)
            .build()

        // UPDATE applies the new interval/constraints immediately without resetting the timer unnecessarily
        workManager.enqueueUniquePeriodicWork(
            "auto_wallpaper",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}