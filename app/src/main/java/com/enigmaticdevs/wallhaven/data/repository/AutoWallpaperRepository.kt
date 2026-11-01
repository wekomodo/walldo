package com.enigmaticdevs.wallhaven.data.repository

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey


data class AutoWallpaperState(
    val switch : String = "off",
    val condition : Conditions = Conditions(onCharging = false, onWifi = false, onIdle = false),
    val interval : String = "",
    val pauseOnLowBattery : Boolean = true,
    val ifLowOnStorage : Boolean = true
)

data class Conditions(
    val onCharging : Boolean,
    val onWifi : Boolean,
    val onIdle : Boolean
)

private object AutoWallpaperKeys{
    val SWITCH = booleanPreferencesKey("auto_wallpaper_switch")
    val ON_CHARGING = booleanPreferencesKey("auto_wallpaper_charging")
    val ON_WIFI = booleanPreferencesKey("auto_wallpaper_wifi")
    val IDLE = booleanPreferencesKey("auto_wallpaper_idle")
    val INTERVAL = stringPreferencesKey("auto_wallpaper_interval")
}


class AutoWallpaperRepository {
}