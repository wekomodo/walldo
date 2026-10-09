package com.enigmaticdevs.wallhaven.domain.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.enigmaticdevs.wallhaven.data.model.Wallpaper
import com.enigmaticdevs.wallhaven.data.model.local.WallhavenAPIparams
import com.enigmaticdevs.wallhaven.domain.repository.GlobalFilterRepository
import com.enigmaticdevs.wallhaven.domain.repository.WallpaperRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


// no longer needed with paging3 since it contains loadstates
/*
@Immutable // Optimizes Jetpack Compose rendering performance
sealed interface WallpaperUiState {

    // 1. Initial State before any data starts loading
    object Idle : WallpaperUiState

    // 2. First-time loading state (shows full-screen shimmer or progress bar)
    object Loading : WallpaperUiState

    // 3. Success state containing your data
    data class Success(
        val wallpapers: Wallpapers, // The raw list of wallpapers to display
        val currentPage: Int,             // Tracks the current loaded page
        val isLastPage: Boolean,          // True if there are no more pages to fetch
        val isPaginating: Boolean = false // True if loading the *next* page (shows bottom spinner)
    ) : WallpaperUiState

    // 4. Failure state containing a human-readable message
    data class Error(
        val message: String,
        val isTransient: Boolean = false // True if error happened during pagination (keeps old data on screen)
    ) : WallpaperUiState
}
*/


@AssistedInject
class WallpaperListViewModel(
    @Assisted private val sorting : String,
    private val globalFilterRepository: GlobalFilterRepository,
    private val repository: WallpaperRepository
) : ViewModel() {
    private val TAG = "WallpaperListViewModel"

    @OptIn(ExperimentalCoroutinesApi::class)
    val wallpaperFLow : Flow<PagingData<Wallpaper>> = globalFilterRepository.filters.map {
        filters ->
        WallhavenAPIparams(
            sorting = sorting,
            purity = filters.purity,
            category = filters.category,
            ratio = filters.ratio,
            resolution = filters.resolution,
            topRange =  filters.topRange,
            page = 1
        )
    }.flatMapLatest { mergedParams ->
        repository.getWallpapersBySort(mergedParams)
    }.cachedIn(viewModelScope)

    /*fun getWallpapersBySort(params : WallhavenAPIparams){
        viewModelScope.launch(Dispatchers.IO) {

            // calls the api to fetch wallpapers
            val networkResult = repository.getWallpapersBySort(params)

            *//*
            _uiState.value = WallpaperUiState.Loading

            networkResult.onSuccess { it ->
                _uiState.value = WallpaperUiState.Success(
                    wallpapers = it,
                    currentPage = it.meta.current_page,
                    isLastPage = it.meta.current_page == it.meta.last_page,
                    isPaginating = false
                )

            }.onFailure { throwable ->
                _uiState.value = WallpaperUiState.Error("Something went wrong")
                throwable.message?.let { Log.e(TAG+"Error",it) }
            }*//*
        }
    }*/


    @AssistedFactory
    @ManualViewModelAssistedFactoryKey(Factory::class) // ← Factory::class
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(@Assisted sorting: String): WallpaperListViewModel
    }

}