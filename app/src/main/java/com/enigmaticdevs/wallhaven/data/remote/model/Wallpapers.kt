package com.enigmaticdevs.wallhaven.data.remote.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Immutable
@Serializable
data class Wallpapers(
    @SerialName("data")
    val wallpaperList : List<Wallpaper>,
    val meta : Meta
)

@Serializable
data class Meta(
    val current_page : Int = 1,
    val last_page : Int = 1,
    val per_page : Int = 24,
    val total : Int = 1
)
