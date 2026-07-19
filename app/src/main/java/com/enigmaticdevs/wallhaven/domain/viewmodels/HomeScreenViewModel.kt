package com.enigmaticdevs.wallhaven.domain.viewmodels

import androidx.lifecycle.ViewModel
import com.enigmaticdevs.wallhaven.data.model.Wallpapers
import com.enigmaticdevs.wallhaven.di.AppGraph
import com.enigmaticdevs.wallhaven.domain.repository.WallpaperRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


data class HomeState(
    val wallpapers : Wallpapers = Wallpapers(emptyList())
)

@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey(HomeScreenViewModel::class)
class HomeScreenViewModel(
    private val repository: WallpaperRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state : StateFlow<HomeState> = _state

    init {

    }


}