package com.enigmaticdevs.wallhaven.domain.repository

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.enigmaticdevs.wallhaven.data.Objects.TopRange
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class FilterState(
    val purity: String = "100",
    val category: String = "110",
    val ratio: String = "",
    val resolution: String = "",
    val topRange: String = TopRange.oneYear,
    val isLoaded: Boolean = false //  false until Datastore is emitted once
)


@Inject
@SingleIn(AppScope::class)
class GlobalFilterRepository(
    private val dataStore: DataStore<Preferences>,
    private val scope: CoroutineScope
) {

    private object FilterKeys {
        val PURITY = stringPreferencesKey("filter_purity")
        val CATEGORY = stringPreferencesKey("filter_category")
        val RATIO = stringPreferencesKey("filter_ratio")
        val RESOLUTION = stringPreferencesKey("filter_resolution")
        val TOP_RANGE = stringPreferencesKey("filter_topRange")
    }

    private val defaults = FilterState()
    val filters: StateFlow<FilterState> = dataStore.data.catch {
        if (it is IOException) emit(emptyPreferences()) else throw it
    }.map { p ->
        FilterState(
            purity = p[FilterKeys.PURITY] ?: defaults.purity,
            category = p[FilterKeys.CATEGORY] ?: defaults.category,
            ratio = p[FilterKeys.RATIO] ?: defaults.ratio,
            resolution = p[FilterKeys.RESOLUTION] ?: defaults.resolution,
            topRange = p[FilterKeys.TOP_RANGE] ?: defaults.topRange,
            isLoaded = true
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = defaults
    )

    private fun save(block: (MutablePreferences) -> Unit) {
        scope.launch { dataStore.edit { block(it) } }
    }

    fun updateCategory(category: String) = save { it[FilterKeys.CATEGORY] = category }

    fun updatePurity(purity: String) = save { it[FilterKeys.PURITY] = purity }

    fun updateResolutions(resolution: String) = save { it[FilterKeys.RESOLUTION] = resolution }

    fun updateRatios(ratio: String) = save { it[FilterKeys.RATIO] = ratio }

    fun updateTopRange(topRange: String) =  save { it[FilterKeys.TOP_RANGE] = topRange }

    fun updateFilters(
        purity: String, category: String, ratio: String, resolution: String, topRange: String
    ) = save {
        it[FilterKeys.PURITY] = purity
        it[FilterKeys.CATEGORY] = category
        it[FilterKeys.RATIO] = ratio
        it[FilterKeys.RESOLUTION] = resolution
        it[FilterKeys.TOP_RANGE] = topRange
    }
}