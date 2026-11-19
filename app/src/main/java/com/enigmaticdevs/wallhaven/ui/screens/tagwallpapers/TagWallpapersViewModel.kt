package com.enigmaticdevs.wallhaven.ui.screens.tagwallpapers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.map
import com.enigmaticdevs.wallhaven.data.remote.model.local.WallhavenAPIparams
import com.enigmaticdevs.wallhaven.data.repository.GlobalFilterRepository
import com.enigmaticdevs.wallhaven.data.repository.WallpaperRepository
import com.enigmaticdevs.wallhaven.domain.pagingsource.toListItem
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.ktor.util.Hash.combine
import io.ktor.util.collections.setValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map


@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class TagWallpapersViewModel(
    private val repository: WallpaperRepository,
    private val globalFilterRepository: GlobalFilterRepository,

) : ViewModel(){

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val wallpapers = combine(
        query,
        globalFilterRepository.filters
    ){ query, filterState ->
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
        .flatMapLatest { (query,params) ->
            repository.getWallpapers(params,query)
        }.map { pagingData ->
        pagingData.map { it.toListItem() }
    }.cachedIn(viewModelScope)

    fun updateQuery(value: String) {
        _query.value = value
    }

}