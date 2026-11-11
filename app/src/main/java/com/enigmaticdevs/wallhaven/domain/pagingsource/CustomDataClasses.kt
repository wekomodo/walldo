package com.enigmaticdevs.wallhaven.domain.pagingsource

import androidx.compose.runtime.Immutable
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionItemEntity
import com.enigmaticdevs.wallhaven.data.remote.model.Wallpaper

@Immutable
data class WallpaperListItem(
    val id: String,
    val path: String,
    val thumbUrl: String,
    val dimensionX : Int,
    val dimensionY : Int
)

fun Wallpaper.toListItem() = WallpaperListItem(
    id = id,
    path = path,
    thumbUrl = thumbs.original,
    dimensionX = dimension_x,
    dimensionY = dimension_y
)

fun CollectionItemEntity.toListItem() = WallpaperListItem(
    id = wallpaperId,
    path = fullUrl.orEmpty(),
    thumbUrl = thumbUrl.orEmpty() ,
    dimensionX = dimensionsX ?: 0,
    dimensionY = dimensionsY ?: 0,
)