package com.enigmaticdevs.wallhaven.ui.screens.features.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionCover
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionDao
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionsDatabase
import com.enigmaticdevs.wallhaven.data.remote.model.RemoteCollection
import com.enigmaticdevs.wallhaven.data.remote.model.RemoteCollections
import com.enigmaticdevs.wallhaven.data.remote.model.Wallpaper
import com.enigmaticdevs.wallhaven.data.remote.model.WallpaperDetail
import com.enigmaticdevs.wallhaven.data.remote.model.Wallpapers
import com.enigmaticdevs.wallhaven.data.repository.AddResult
import com.enigmaticdevs.wallhaven.data.repository.CollectionsRepository
import com.enigmaticdevs.wallhaven.data.repository.GlobalFilterRepository
import com.enigmaticdevs.wallhaven.data.repository.SettingsRepository
import com.enigmaticdevs.wallhaven.data.repository.WallpaperRepository
import com.enigmaticdevs.wallhaven.ui.screens.features.collections.components.CreateCollectionRequest
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class CollectionsViewModel(
    private val collectionsRepository: CollectionsRepository,
    private val repository: WallpaperRepository,
    private val settings: SettingsRepository,
    private val globalFilterRepository: GlobalFilterRepository
) : ViewModel() {


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


    /*   fun fetchCollections(){
           viewModelScope.launch {
               val result = repository.fetchCollections()
               if(result!=null)
                   _collections.value = result
           }
       }*/

}