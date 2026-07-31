package com.enigmaticdevs.wallhaven.data.model

data class Wallpapers(
    val wallpapers : List<Wallpaper>,
    val meta : Meta
)

data class Meta(
    val current_page : Int,
    val last_page : Int,
    val per_page : Int,
    val total : Int
)
