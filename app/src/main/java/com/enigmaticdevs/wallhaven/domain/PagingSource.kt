package com.enigmaticdevs.wallhaven.domain

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.enigmaticdevs.wallhaven.data.model.Wallpaper
import com.enigmaticdevs.wallhaven.data.model.Params
import com.enigmaticdevs.wallhaven.domain.repository.MainRepository
import retrofit2.HttpException

class PagingSource(val respository : MainRepository, val params : Params) : PagingSource<Int, Wallpaper>() {
    override fun getRefreshKey(state: PagingState<Int, Wallpaper>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Wallpaper> {
      return  try {
            // Start refresh at page 1 if undefined.
            val currentPage = params.key ?: 1
            val response = respository.getWallpaperBySort(this.params,currentPage)
          if(response!=null) {
              val data = response.data
              val responseData = mutableListOf<Wallpaper>()
              responseData.addAll(data)

               LoadResult.Page(
                  data = data,
                  prevKey = null, // Only paging forward.
                  nextKey = currentPage.plus(1)
              )
          }
          else
              LoadResult.Page(
                  data = emptyList(),
                  prevKey = null, // Only paging forward.
                  nextKey = currentPage.plus(1)
              )
        } catch (e: HttpException) {
          // HttpException for any non-2xx HTTP status codes.
           LoadResult.Error(e)
      }catch (e: Exception) {
           LoadResult.Error(e)
        }
    }
}