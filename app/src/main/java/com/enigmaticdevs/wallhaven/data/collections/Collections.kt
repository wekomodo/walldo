package com.enigmaticdevs.wallhaven.data.collections

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey(autoGenerate = true) val id : Long = 0,
    val name : String,
    val isDefault : Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "wallpapers")
data class WallpaperEntity(
    @PrimaryKey val wallpaperId : String,
    val thumbUrl : String,
    val fullUrl : String?,
    val resolution : String?,
    val purity : String?
)

// Which wallpaper is in which collection ( a wallpaper can be in several at once)
@Entity(
    tableName = "collection_items",
    primaryKeys = ["collectionId","wallpaperId"],
    foreignKeys = [
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"], childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WallpaperEntity::class,
            parentColumns = ["wallpaperId"], childColumns = ["wallpaperId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("wallpaperId")]
)

data class CollectionItem(
    val collectionId : Long,
    val wallpaperId : String,
    val addedAt : Long = System.currentTimeMillis()
)