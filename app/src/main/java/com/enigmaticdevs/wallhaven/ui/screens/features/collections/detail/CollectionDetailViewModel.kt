package com.enigmaticdevs.wallhaven.ui.screens.features.collections.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionRemoteMediator
import com.enigmaticdevs.wallhaven.data.repository.WallpaperRepository
import com.enigmaticdevs.wallhaven.navigation.Screen
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey


@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class CollectionDetailViewModel(
    private val repository: WallpaperRepository
) : ViewModel(){


}