package com.enigmaticdevs.wallhaven.ui.screens.wallpaperdetail

import android.util.Log
import android.util.LruCache
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enigmaticdevs.wallhaven.data.remote.model.WallpaperDetail
import com.enigmaticdevs.wallhaven.data.repository.AddResult
import com.enigmaticdevs.wallhaven.data.repository.CollectionMembership
import com.enigmaticdevs.wallhaven.data.repository.CollectionsRepository
import com.enigmaticdevs.wallhaven.data.repository.WallpaperRepository
import com.enigmaticdevs.wallhaven.ui.screens.collections.components.CreateCollectionRequest
import com.enigmaticdevs.wallhaven.ui.screens.homescreen.components.QueryNames
import com.enigmaticdevs.wallhaven.ui.screens.homescreen.components.SavedQueries
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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


/*@Immutable
sealed interface WallpaperDetailUiState{


    // 1. Initial State before any data starts loading
    object Idle : WallpaperDetailUiState

    // 2. First-time loading state (shows full-screen shimmer or progress bar)
    object Loading : WallpaperDetailUiState

    // 3. Success state containing your data
    data class Success(
        val wallpapers: Wallpaper, // The raw list of wallpapers to display
        // True if loading the *next* page (shows bottom spinner)
    ) : WallpaperDetailUiState

    // 4. Failure state containing a human-readable message
    data class Error(
        val message: String,
        //val isTransient: Boolean = false // True if error happened during pagination (keeps old data on screen)
    ) : WallpaperDetailUiState

}*/





@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class WallpaperDetailViewModel (
    private val repository: WallpaperRepository,
    private val collectionsRepository: CollectionsRepository,
    private val savedQueries: SavedQueries,
    val shareableWallpapers: ShareableWallpapers
) : ViewModel() {
    private val TAG = "WallpaperDetailViewModel"

    private val _addResultEvent = Channel<AddResult>(Channel.BUFFERED)
    val addResultEvent = _addResultEvent.receiveAsFlow()
    private val _wallpaper = MutableStateFlow<WallpaperDetail?>(null)
    val wallpaper = _wallpaper.asStateFlow()

    // 1. Create a cache that holds a maximum of 30 wallpapers in memory
    private val wallpaperCache = LruCache<String, WallpaperDetail>(30)

    @OptIn(ExperimentalCoroutinesApi::class)
    val memberships: StateFlow<List<CollectionMembership>> = wallpaper
        .filterNotNull()
        .flatMapLatest { collectionsRepository.observeMemberships(it.data.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val isFavorite: StateFlow<Boolean> = memberships
        .map { list -> list.any { !it.collection.isSynced && it.contains } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
    fun setMembership(collectionId: Long, shouldContain: Boolean) {
        val wallpaper = wallpaper.value?.data ?: return
        viewModelScope.launch {
            if(shouldContain)
            {
            when(val result = collectionsRepository.addWallpaper(wallpaper, collectionId)){
                is AddResult.NeedsMove ->{
                    //for radiobutton events
                    collectionsRepository.moveWallpaper(wallpaper,collectionId)
                }
                else -> _addResultEvent.send(result)
            }
            }
            else {
                collectionsRepository.removeWallpaper(wallpaper.id, collectionId)
            }
        }
    }

    fun createCollectionAndAdd(it : CreateCollectionRequest) {
        val wallpaper = wallpaper.value?.data ?: return
        viewModelScope.launch {
            val id = runCatching {collectionsRepository.createCollection(it.name, synced = it.isSynced, isPublic = it.isPublic) }.getOrNull() ?: return@launch
            val result = collectionsRepository.addWallpaper(wallpaper, id)
            _addResultEvent.send(result)
        }
    }
    fun searchTag(tag: String) = savedQueries.set(QueryNames.HOME, tag)
    fun getWallpaperDetails(id : String) {

        wallpaperCache.get(id)?.let { cachedWallpaper ->
            _wallpaper.value = cachedWallpaper
            Log.d(TAG, "this value was in lruCache already! ")
            return
        }
        viewModelScope.launch {
            _wallpaper.value = null
            val networkRequest = repository.getWallpaperDetail(id)
            networkRequest?.let {
                // caching the new request
                wallpaperCache.put(id, it)
                _wallpaper.value = it
            }
        }
    }

  /*  private fun WallpaperDetail.toEntity() = WallpaperEntity(
        wallpaperId = data.id,
        thumbUrl = data.thumbs.large,   // guess: use your model's thumbnail field
        fullUrl = data.path,
        resolution = data.resolution,   // guess
        purity = data.purity            // guess
    )*/



}