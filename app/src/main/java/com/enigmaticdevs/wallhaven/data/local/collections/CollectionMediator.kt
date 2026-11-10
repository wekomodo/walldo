package com.enigmaticdevs.wallhaven.data.local.collections

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.enigmaticdevs.wallhaven.data.remote.WallhavenAPI

@OptIn(ExperimentalPagingApi::class)
class CollectionRemoteMediator(
    private val collectionId: Long,
    private val wallhavenId: Long, // The remote ID needed for the API
    private val api: WallhavenAPI,
    private val dao: CollectionDao,
    private val username : String,
) : RemoteMediator<Int, CollectionItemEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, CollectionItemEntity>
    ): MediatorResult {
        return try {
            // 1. Determine which page to load based on LoadType (REFRESH, PREPEND, APPEND)
            val page = when (loadType) {
                LoadType.REFRESH -> 1
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    // Calculate next page based on current item count in DB
                    val itemCount = dao.getItemCount(collectionId)
                    (itemCount / 24) + 1
                }
            }

            // 2. Fetch from Wallhaven
            val response = api.fetchCollectionWallpapers(wallhavenId, username = username,page)

            // 3. Insert into Room (Room is the single source of truth)
            val newItems = response.wallpaperList.mapIndexed { index, wallpaper ->
                CollectionItemEntity(
                    wallpaperId = wallpaper.id,
                    collectionId = collectionId,
                    thumbUrl = wallpaper.thumbs.original,
                    syncedSlot = (page - 1) * 24 + index
                )
            }
        //    dao.insertCollectionItems(newItems)

            // 4. Tell Paging3 if we hit the end of the collection
            MediatorResult.Success(endOfPaginationReached = response.wallpaperList.isEmpty())

        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}