package com.enigmaticdevs.wallhaven.data.model

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
    val current_page : Int,
    val last_page : Int,
    val per_page : Int,
    val total : Int
)
