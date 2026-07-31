package com.enigmaticdevs.wallhaven.domain.viewmodels


import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.enigmaticdevs.wallhaven.data.Objects.Sorting
import com.enigmaticdevs.wallhaven.data.model.Wallpaper
import com.enigmaticdevs.wallhaven.data.model.local.WallhavenAPIparams
import com.enigmaticdevs.wallhaven.domain.repository.WallpaperRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactoryKey
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Immutable // Optimizes Jetpack Compose rendering performance
sealed interface WallpaperUiState {

    // 1. Initial State before any data starts loading
    object Idle : WallpaperUiState

    // 2. First-time loading state (shows full-screen shimmer or progress bar)
    object Loading : WallpaperUiState

    // 3. Success state containing your data
    data class Success(
        val wallpapers: List<Wallpaper>, // The raw list of wallpapers to display
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


@AssistedInject
class WallpaperListViewModel(
    @Assisted private val sorting : String,
    private val repository: WallpaperRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WallpaperUiState>(WallpaperUiState.Loading)
    val uiState : StateFlow<WallpaperUiState> = _uiState

    init {

    }

    suspend fun getWallpapersBySort(params : WallhavenAPIparams){
        viewModelScope.launch {
            _uiState.value = WallpaperUiState.Loading
        }
        // calls the api to fetch wallpapers
        val networkResult = repository.getWallpapersBySort(params)

        networkResult.onSuccess { it->
            _uiState.value = WallpaperUiState.Success(wallpapers = it.wallpapers,
                currentPage = it.meta.current_page,
                isLastPage = it.meta.current_page== it.meta.last_page,
                isPaginating = false)

            Log.d("Viewmodel Homescreen",it.toString())
        }


    }


    @AssistedFactory
    @ViewModelAssistedFactoryKey(WallpaperListViewModel::class) // ← the VM class, not Factory::class
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ViewModelAssistedFactory {
        override fun create(extras: CreationExtras): WallpaperListViewModel {
            val sorting = extras[SortingKey] ?: Sorting.topList
            return create(sorting)
        }
        fun create(@Assisted sorting: String): WallpaperListViewModel
    }

    companion object {
        val SortingKey = object : CreationExtras.Key<String> {}
    }


}