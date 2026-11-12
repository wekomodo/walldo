package com.enigmaticdevs.wallhaven.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


data class AutoWallpaperState(
    val switch : Boolean = false,
    val source : String = "",
    val interval : String = AutoWallpaperInterval.MINS_15.name,
    val onWifi: Boolean = false,
    val onCharging: Boolean = false,
    val onIdle : Boolean = false,
   // val condition : Conditions = Conditions(onCharging = false, onWifi = false, onIdle = false),
    val orientationFix : Boolean = true,
    val pauseOnLowBattery : Boolean = true,
    val ifLowOnStorage : Boolean = true,
    val screenSelect: String = "both"
)

data class Conditions(
    val onCharging : Boolean,
    val onWifi : Boolean,
    val onIdle : Boolean
)

private object AutoWallpaperKeys{
    val SWITCH = booleanPreferencesKey("auto_wallpaper_switch")
    val ON_CHARGING = booleanPreferencesKey("auto_wallpaper_charging")
    val PAUSE_ON_BATTERY_LOW = booleanPreferencesKey("auto_wallpaper_battery_low")
    val ON_WIFI = booleanPreferencesKey("auto_wallpaper_wifi")
    val ON_IDLE = booleanPreferencesKey("auto_wallpaper_idle")
    val INTERVAL = stringPreferencesKey("auto_wallpaper_interval")
    val STORAGE_LOW = booleanPreferencesKey("auto_wallpaper_storage_low")

    val ORIENTATION_FIX = booleanPreferencesKey("auto_wallpaper_orientation_fix")

    val SCREEN = stringPreferencesKey("autoWallpaper_select_screen")
}


@Inject
class AutoWallpaperRepository(
    private val dataStore: DataStore<Preferences>,
    private val scope : CoroutineScope
) {

    private val defaults = AutoWallpaperState()

    val state : StateFlow<AutoWallpaperState> = dataStore.data.catch {
        if(it is IOException) emit(emptyPreferences()) else throw it
    }.map {
        AutoWallpaperState(
            switch = it[AutoWallpaperKeys.SWITCH] ?: defaults.switch,
            onWifi = it[AutoWallpaperKeys.ON_WIFI] ?: defaults.onWifi,
            onCharging = it[AutoWallpaperKeys.ON_CHARGING] ?: defaults.onCharging,
            pauseOnLowBattery = it[AutoWallpaperKeys.PAUSE_ON_BATTERY_LOW] ?: defaults.pauseOnLowBattery,
            onIdle = it[AutoWallpaperKeys.ON_IDLE] ?: defaults.onIdle,
            interval = it[AutoWallpaperKeys.INTERVAL] ?: defaults.interval,
            ifLowOnStorage = it[AutoWallpaperKeys.STORAGE_LOW] ?: defaults.ifLowOnStorage,
            orientationFix = it[AutoWallpaperKeys.ORIENTATION_FIX] ?: defaults.orientationFix,
            screenSelect = it[AutoWallpaperKeys.SCREEN] ?: defaults.screenSelect
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = defaults
    )


    private fun save( block : (MutablePreferences) -> Unit) {
        scope.launch { dataStore.edit { block(it) } }
    }

    fun updateSwitch(switch : Boolean) = save { it[AutoWallpaperKeys.SWITCH] = switch }
    fun updateOnCharging(switch : Boolean) = save { it[AutoWallpaperKeys.ON_CHARGING] = switch }

    fun updatePauseOnBatteryLow (switch: Boolean) = save { it[AutoWallpaperKeys.PAUSE_ON_BATTERY_LOW] = switch }
    fun updateOnWifi(switch : Boolean) = save { it[AutoWallpaperKeys.ON_WIFI] = switch }
    fun updateOnIdle(switch : Boolean) = save { it[AutoWallpaperKeys.ON_IDLE] = switch }

    fun updateInterval(value: String) = save { it[AutoWallpaperKeys.INTERVAL] = value }

    fun updateIfLowOnStorage(value: Boolean) = save { it[AutoWallpaperKeys.STORAGE_LOW] = value }

    fun updateOrientationFix(value: Boolean) = save { it[AutoWallpaperKeys.ORIENTATION_FIX] = value }

    fun updateScreenSelect(value : String) = save { it[AutoWallpaperKeys.SCREEN] = value }



    // this was a very dumb try to reduce code lol
    /*private fun update(value : Boolean, key : Preferences.Key<Boolean>) = save {
        it[key] = value
    }*/
}


enum class AutoWallpaperInterval(val value: Long, val timeUnit: TimeUnit) {
    MINS_15(15, TimeUnit.MINUTES),
    MINS_30(30, TimeUnit.MINUTES),
    HOUR_1(1, TimeUnit.HOURS),
    HOURS_4(4, TimeUnit.HOURS),
    HOURS_12(12, TimeUnit.HOURS),
    DAY_1(1, TimeUnit.DAYS);

    companion object {
        // Fallback safety if the stored string doesn't match
        fun fromName(name: String?): AutoWallpaperInterval {
            return entries.find { it.name == name } ?: HOUR_1
        }
    }
}

