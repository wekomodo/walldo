package com.enigmaticdevs.wallhaven.response

import com.enigmaticdevs.wallhaven.response.Data
import com.enigmaticdevs.wallhaven.response.Meta

data class Wallpaper(
    val data: List<Data>,
    val meta: Meta
)