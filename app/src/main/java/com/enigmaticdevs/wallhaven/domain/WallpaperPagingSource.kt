package com.enigmaticdevs.wallhaven.domain

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.enigmaticdevs.wallhaven.data.Objects.Sorting
import com.enigmaticdevs.wallhaven.data.model.Wallpaper
import com.enigmaticdevs.wallhaven.data.model.Wallpapers
import com.enigmaticdevs.wallhaven.data.model.local.Category
import com.enigmaticdevs.wallhaven.data.model.local.Purity
import com.enigmaticdevs.wallhaven.data.remote.WallhavenAPI

/*
class WallpaperPagingSource(
    private val api: WallhavenAPI,
    private val sorting : Sorting,
    private val purity: Purity,
    private val category: Category
) : PagingSource<Int,Wallpaper>(){

    override fun getRefreshKey(state : PagingState<Int, Wallpapers>) : Int? {

        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?:state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }

}*/
