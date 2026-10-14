package com.enigmaticdevs.wallhaven.domain.repository

import com.enigmaticdevs.wallhaven.data.Objects.TopRange
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


data class FilterState(
    val purity : String = "100",
    val category : String = "110",
    val ratio : String = "",
    val resolution : String = "",
    val topRange : String = TopRange.oneYear
)


@Inject
@SingleIn(AppScope::class)
class GlobalFilterRepository {

    private val _filters = MutableStateFlow(FilterState())
    val filters : StateFlow<FilterState> = _filters.asStateFlow()


    fun updateCategory(category : String){
        _filters.update { it.copy(category = category) }
    }

    fun updatePurity(purity: String){
        _filters.update { it.copy(purity = purity) }
    }

    fun updateResolutions(resolution: String){
        _filters.update { it.copy(resolution = resolution) }
    }

    fun updateRatios(ratio: String){
        _filters.update { it.copy(ratio = ratio) }
    }

    fun updateTopRange(topRange: String){
        _filters.update { it.copy(topRange = topRange) }
    }

    fun updateFilters(purity : String,category: String, ratio : String, resolution : String, topRange : String){
        _filters.value = FilterState(purity,category,ratio,resolution,topRange)
    }

}