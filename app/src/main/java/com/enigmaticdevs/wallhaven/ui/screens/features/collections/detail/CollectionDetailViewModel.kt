package com.enigmaticdevs.wallhaven.ui.screens.features.collections.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.map
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionsDatabase
import com.enigmaticdevs.wallhaven.data.repository.GlobalFilterRepository
import com.enigmaticdevs.wallhaven.data.repository.WallpaperRepository
import com.enigmaticdevs.wallhaven.domain.pagingsource.toListItem
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map


@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class CollectionDetailViewModel(
    private val globalFilterRepository: GlobalFilterRepository,
    private val database : CollectionsDatabase,
    private val repository: WallpaperRepository
) : ViewModel(){

    // -1L acts as an initial empty state
    private val _collectionId = MutableStateFlow(-1L)

    @OptIn(ExperimentalCoroutinesApi::class)
    val wallpapersFlow = combine(
        _collectionId,
        globalFilterRepository.filters)
    { id, filters ->
        id to filters.purity
    }
        .filter { (id,_) -> id!=-1L }// Ignore the initial state
        .distinctUntilChanged()
        .flatMapLatest { (id, purity) ->
            Pager(
                config = PagingConfig(pageSize = 24, enablePlaceholders = true),
                pagingSourceFactory = { database.collectionDao().getPagedWallpapers(id, purity = purity) }
            ).flow.map { pagingData ->
                pagingData.map { entity -> entity.toListItem() }
            }
        }.cachedIn(viewModelScope)


    fun setCollectionId(id: Long) {
        if (_collectionId.value != id) {
            _collectionId.value = id
        }
    }

}