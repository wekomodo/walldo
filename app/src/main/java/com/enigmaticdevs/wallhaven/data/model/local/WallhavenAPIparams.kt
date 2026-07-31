package com.enigmaticdevs.wallhaven.data.model.local

import com.enigmaticdevs.wallhaven.data.Objects.Sorting
import com.enigmaticdevs.wallhaven.data.Objects.TopRange

data class WallhavenAPIparams(
    val sorting: String,
    val purity: String,
    val category: String,
    val topRange: String,
    val ratio: String,
    val resolution: String,
    val page: Int
)