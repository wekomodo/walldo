package com.enigmaticdevs.wallhaven.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Photo (
    val data : Wallpaper
) : java.io.Serializable