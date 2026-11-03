package com.enigmaticdevs.wallhaven.data.local.collections

data class RemoteCollection(
    val id: Long,
    val label: String,
    /** The user's default collection on the site. It can't be deleted. */
    val isDefault: Boolean,
)

/**
 * The READ side, via the official API with the user's key.
 * Implement this on top of the API client you already have.
 */
interface WallhavenCollectionsRemote {
    suspend fun fetchCollections(): List<RemoteCollection>

    /** Every wallpaper ID in one collection, all pages. */
    suspend fun fetchWallpaperIds(collectionId: Long): List<String>
}