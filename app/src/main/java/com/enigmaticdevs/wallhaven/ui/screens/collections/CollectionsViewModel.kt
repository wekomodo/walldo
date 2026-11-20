package com.enigmaticdevs.wallhaven.ui.screens.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionSyncManager
import com.enigmaticdevs.wallhaven.data.local.collections.SyncState
import com.enigmaticdevs.wallhaven.data.remote.model.RemoteCollections
import com.enigmaticdevs.wallhaven.data.remote.model.Wallpapers
import com.enigmaticdevs.wallhaven.data.repository.CollectionsRepository
import com.enigmaticdevs.wallhaven.data.repository.GlobalFilterRepository
import com.enigmaticdevs.wallhaven.data.repository.GlobalFlagsRepository
import com.enigmaticdevs.wallhaven.data.repository.SettingsRepository
import com.enigmaticdevs.wallhaven.data.repository.WallpaperRepository
import com.enigmaticdevs.wallhaven.ui.screens.collections.components.CreateCollectionRequest
import com.enigmaticdevs.wallhaven.work.CollectionSyncWorker
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
class CollectionsViewModel(
    private val collectionsRepository: CollectionsRepository,
    private val repository: WallpaperRepository,
    private val syncManager : CollectionSyncManager,
    private val settings: SettingsRepository,
    private val globalFilterRepository: GlobalFilterRepository,
    private val globalFlagsRepository: GlobalFlagsRepository,
    private val workManager: WorkManager
) : ViewModel() {

    // Track ongoing progress
    val syncState = syncManager.syncState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SyncState(
            pushing = false,
            pulling = false,
            doneCollections = 0,
            totalCollections = 0
        )
    )

    val isPro = globalFlagsRepository.isPro.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

  //  val syncStates = syncManager.syncState

    //pull to refresh box
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    // Track the final result for error handling
    val lastSyncResult = syncManager.lastResult
    private val _collections = MutableStateFlow<RemoteCollections?>(null)
    val collections = _collections.asStateFlow()

    private val _collectionContents = MutableStateFlow<Wallpapers?>(null)
    val collectionContents = _collectionContents.asStateFlow()

    val username: StateFlow<String> = settings.usernameFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ""
    )

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

    /*init {
        fetchCollections()
    }*/

    fun fetchCollectionWallpaper(id: Long, page: Int) {
        viewModelScope.launch {
            val currentUsername = username.value
            val result = repository.fetchCollectionWallpapers(id, currentUsername, page)
            _collectionContents.value = result
        }
    }


    fun updateSortOrder(order : List<Long>) {
        viewModelScope.launch {
            collectionsRepository.updateSortOrder(order)
        }
    }

    fun createCollection(it : CreateCollectionRequest) {
        //val wallpaper = wallpaper.value?.data ?: return
        viewModelScope.launch {
            runCatching {
                collectionsRepository.createCollection(it.name, synced = it.isSynced, isPublic = it.isPublic)
            }.getOrNull() ?: return@launch
            // val result = collectionsRepository.addWallpaper(wallpaper, id)
            // _addResultEvent.send(result)
        }
    }

    fun refresh(){
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                syncManager.requestSync()
                // 2. Enqueue background worker to finish pulling any multi-page collections
                val request = OneTimeWorkRequestBuilder<CollectionSyncWorker>()
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .build()

                workManager.enqueueUniqueWork(
                    "collection_sync",
                    ExistingWorkPolicy.KEEP,
                    request
                )
            } finally {
                // Spinner dismisses as soon as the fast phase finishes;
                // SyncProgressIndicator tracks the remaining pages
                _isRefreshing.value = false
            }
        }
    }


    /*   fun fetchCollections(){
           viewModelScope.launch {
               val result = repository.fetchCollections()
               if(result!=null)
                   _collections.value = result
           }
       }*/

}