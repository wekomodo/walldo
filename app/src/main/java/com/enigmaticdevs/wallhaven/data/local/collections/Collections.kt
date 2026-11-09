package com.enigmaticdevs.wallhaven.data.local.collections

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "collection", indices = [Index("remoteId")])
data class CollectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** true = mirrors a wallhaven collection (pro only). false = lives only on this device. */
    val isSynced: Boolean,
    /** wallhaven's collection ID. Null for local collections, and for synced ones not created on the site yet. */
    val remoteId: Long? = null,
    /** when was this collection created */
    val createdAt: Long = System.currentTimeMillis()
)

// Which wallpaper is in which collection ( a wallpaper can be in several at once if local only)
@Entity(
    tableName = "collection_item",
    primaryKeys = ["wallpaperId", "collectionId"],
    foreignKeys = [
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("collectionId"),
        Index(value = ["wallpaperId", "syncedSlot"], unique = true)
    ]
)
data class CollectionItemEntity(
    val wallpaperId: String,
    val collectionId: Long,

    //offline caching of wallpaper data
    val thumbUrl : String? = null,
    val fullUrl : String? = null,
    val resolution : String? = null,
    val purity : String? = null,
    val syncedSlot: Int? = null,
    val addedAt: Long = System.currentTimeMillis()
)

enum class OpType { CREATE_COLLECTION, DELETE_COLLECTION, ADD, REMOVE }

/** PENDING ops are sent in order. REJECTED ops were refused by the site and are kept for the UI. */
enum class OpState { PENDING, REJECTED }

@Entity(tableName = "pending_op")
data class PendingOpEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: OpType,
    val state: OpState = OpState.PENDING,
    /** Local collection ID (CREATE_COLLECTION, ADD). */
    val collectionId: Long? = null,
    /** DELETE_COLLECTION only: the local row is already gone by the time this runs. */
    val remoteCollectionId: Long? = null,
    /** ADD, REMOVE. */
    val wallpaperId: String? = null,
    /** CREATE_COLLECTION only. */
    val label: String? = null,
    val isPublic: Boolean = false,
    val lastError: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

data class CollectionWithCount(
    @Embedded val collection: CollectionEntity,
    val itemCount: Int
)