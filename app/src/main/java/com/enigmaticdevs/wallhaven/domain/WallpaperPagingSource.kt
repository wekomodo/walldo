package com.enigmaticdevs.wallhaven.domain

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.enigmaticdevs.wallhaven.data.Objects.Sorting
import com.enigmaticdevs.wallhaven.data.model.Wallpaper
import com.enigmaticdevs.wallhaven.data.model.Wallpapers
import com.enigmaticdevs.wallhaven.data.model.local.Category
import com.enigmaticdevs.wallhaven.data.model.local.Purity
import com.enigmaticdevs.wallhaven.data.model.local.WallhavenAPIparams
import com.enigmaticdevs.wallhaven.data.remote.WallhavenAPI

class WallpaperPagingSource(
    private val api: WallhavenAPI,
    private val apiParams: WallhavenAPIparams
) : PagingSource<Int,Wallpaper>(){
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Wallpaper> {
        val currentPage = params.key ?: 1

        return try {
            val response = api.getWallpapersBySort(
                sorting = apiParams.sorting,
                purity = apiParams.purity,
                category = apiParams.category,
                topRange = apiParams.topRange,
                ratio = apiParams.ratio,
                resolution = apiParams.resolution,
                page = currentPage
            )
            val wallpapers = response.wallpaperList
            val meta = response.meta

            //return success with pointers to prev/next pages
            LoadResult.Page(
                data = wallpapers,
                prevKey = if(currentPage==1) null else currentPage-1,
                nextKey = if(currentPage>=meta.last_page || wallpapers.isEmpty()) null else currentPage + 1
            )

        }
        catch (exception : Exception){
            LoadResult.Error(exception)


        }
    }

    override fun getRefreshKey(state: PagingState<Int, Wallpaper>): Int? {
        return state.anchorPosition?.let {
            state.closestPageToPosition(it)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition = it)?.nextKey?.minus(1)
        }
    }


}
