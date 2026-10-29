package com.enigmaticdevs.wallhaven.ui.screens.homescreen.bottomsheet

import androidx.lifecycle.ViewModel
import com.enigmaticdevs.wallhaven.data.repository.FilterState
import com.enigmaticdevs.wallhaven.data.repository.GlobalFilterRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.StateFlow

@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey(FilterViewModel::class)
class FilterViewModel(private val repository: GlobalFilterRepository) : ViewModel() {

    val filters: StateFlow<FilterState> = repository.filters

    fun updateCategory(value: String) = repository.updateCategory(value)
    fun updatePurity(value: String) = repository.updatePurity(value)
    fun updateRatios(value: String) = repository.updateRatios(value)
    fun updateResolutions(value: String) = repository.updateResolutions(value)
}