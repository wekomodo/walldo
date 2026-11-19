package com.enigmaticdevs.wallhaven.ui.screens.homescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.enigmaticdevs.wallhaven.data.remote.model.local.WallhavenAPIparams
import com.enigmaticdevs.wallhaven.data.repository.GlobalFilterRepository
import com.enigmaticdevs.wallhaven.data.repository.WallpaperRepository
import com.enigmaticdevs.wallhaven.domain.pagingsource.toListItem
import com.enigmaticdevs.wallhaven.ui.screens.homescreen.components.QueryNames
import com.enigmaticdevs.wallhaven.ui.screens.homescreen.components.SavedQueries
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class HomeViewModel(
    val globalFilterRepository: GlobalFilterRepository,
    private val repository: WallpaperRepository,
    private val savedQueries: SavedQueries

) : ViewModel() {

    //Search Query State (blank by default
    val savedQuery: StateFlow<String?> = savedQueries.observe(QueryNames.HOME)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    private val query = savedQuery.filterNotNull()

    //sort chip State
    /* private val _activeSort = MutableStateFlow(Sorting.topList)
     val activeSort = _activeSort.asStateFlow()*/

    @OptIn(ExperimentalCoroutinesApi::class)
    val wallpapers = combine(
        query,
        globalFilterRepository.filters
    ) { query, filterState ->

        query to WallhavenAPIparams(
            purity = filterState.purity,
            category = filterState.category,
            ratio = filterState.ratio,
            resolution = filterState.resolution,
            topRange = filterState.topRange,
            sorting = filterState.sorting
        )
    }
        .distinctUntilChanged()
        .flatMapLatest { (query, params) ->
            // Triggers the repository automatically whenever any of the 3 states change
            repository.getWallpapers(params, query)
        }.map { pagingData ->
            pagingData.map { it.toListItem() }
        }.cachedIn(viewModelScope)

    fun triggerSearch(query: String) = savedQueries.set(QueryNames.HOME, query)

    fun updateSort(newSort: String) {
        globalFilterRepository.updateSort(newSort)
    }

}