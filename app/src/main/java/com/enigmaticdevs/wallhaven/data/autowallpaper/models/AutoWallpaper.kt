package com.enigmaticdevs.wallhaven.data.autowallpaper.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.enigmaticdevs.wallhaven.util.Constant

@Entity(tableName = Constant.AutoWallpaperHistoryTableName)
data class AutoWallpaper(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "imageId") val imageId: String?,
    @ColumnInfo(name = "thumbs") val thumbs: String?,
    @ColumnInfo(name = "imageUrl") val ImageUrl : String?,
    @ColumnInfo(name = "purity") val purity : String?,
    @ColumnInfo(name = "dimension_x") val dimension_x : Int?,
    @ColumnInfo(name = "dimension_y") val dimension_y : Int?
)