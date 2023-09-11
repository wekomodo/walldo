package com.enigmaticdevs.wallhaven.domain

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.enigmaticdevs.wallhaven.data.model.Params
import com.enigmaticdevs.wallhaven.data.model.Wallpaper
import com.enigmaticdevs.wallhaven.domain.repository.MainRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class PagingSource(
    private val repository: MainRepository,
    private val query : String,
    private val sorting: String,
    private val topRange: String,
    private val param: Params
) : PagingSource<Int, Wallpaper>() {
    override fun getRefreshKey(state: PagingState<Int, Wallpaper>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Wallpaper> {
        return withContext(Dispatchers.IO) {
            try {
                // Start refresh at page 1 if undefined.
                val currentPage = params.key ?: 1
                val response =
                    repository.getSearchWallpapers(query, sorting, topRange, param, currentPage)
                if (response != null) {
                    val data = response.data
                    if (data.size > 0) {
                        val responseData = mutableListOf<Wallpaper>()
                        responseData.addAll(data)
                        LoadResult.Page(
                            data = data,
                            prevKey = null, // Only paging forward.
                            nextKey = currentPage.plus(1)
                        )
                    } else {
                        LoadResult.Page(
                            data = emptyList(),
                            prevKey = null, // Only paging forward.
                            nextKey = null
                        )
                    }
                } else
                    LoadResult.Page(
                        data = emptyList(),
                        prevKey = null, // Only paging forward.
                        nextKey = null
                    )
            } catch (e: HttpException) {
                // HttpException for any non-2xx HTTP status codes.
                LoadResult.Error(e)
            } catch (e: Exception) {
                LoadResult.Error(e)
            }
        }
    }
}