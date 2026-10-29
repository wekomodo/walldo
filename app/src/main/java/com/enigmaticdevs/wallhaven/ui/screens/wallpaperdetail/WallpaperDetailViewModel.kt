package com.enigmaticdevs.wallhaven.ui.screens.wallpaperdetail

import android.util.Log
import android.util.LruCache
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enigmaticdevs.wallhaven.data.collections.WallpaperEntity
import com.enigmaticdevs.wallhaven.data.model.WallpaperDetail
import com.enigmaticdevs.wallhaven.domain.repository.CollectionMembership
import com.enigmaticdevs.wallhaven.domain.repository.CollectionsRepository
import com.enigmaticdevs.wallhaven.domain.repository.WallpaperRepository
import com.enigmaticdevs.wallhaven.ui.screens.homescreen.components.QueryNames
import com.enigmaticdevs.wallhaven.ui.screens.homescreen.components.SavedQueries
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
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
@ViewModelKey(WallpaperDetailViewModel::class)
class WallpaperDetailViewModel (
    private val repository: WallpaperRepository,
    private val collectionsRepository: CollectionsRepository,
    private val savedQueries: SavedQueries,
    val shareableWallpapers: ShareableWallpapers
) : ViewModel() {
    private val TAG = "WallpaperDetailViewModel"
    private val _wallpaper = MutableStateFlow<WallpaperDetail?>(null)
    val wallpaper = _wallpaper.asStateFlow()

    // 1. Create a cache that holds a maximum of 30 wallpapers in memory
    private val wallpaperCache = LruCache<String, WallpaperDetail>(30)

    @OptIn(ExperimentalCoroutinesApi::class)
    val memberships: StateFlow<List<CollectionMembership>> = wallpaper
        .filterNotNull()
        .flatMapLatest { collectionsRepository.observeMemberships(it.data.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setMembership(collectionId: Long, shouldContain: Boolean) {
        val current = wallpaper.value ?: return
        viewModelScope.launch {
            collectionsRepository.setMembership(current.toEntity(), collectionId, shouldContain)
        }
    }

    fun createCollectionAndAdd(name: String) {
        val current = wallpaper.value ?: return
        viewModelScope.launch {
            val id = collectionsRepository.createCollection(name) ?: return@launch
            collectionsRepository.setMembership(current.toEntity(), id, true)
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

    private fun WallpaperDetail.toEntity() = WallpaperEntity(
        wallpaperId = data.id,
        thumbUrl = data.thumbs.large,   // guess: use your model's thumbnail field
        fullUrl = data.path,
        resolution = data.resolution,   // guess
        purity = data.purity            // guess
    )



}