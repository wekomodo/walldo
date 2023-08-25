package com.enigmaticdevs.wallhaven.domain

import android.util.Log
import android.widget.Toast
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.enigmaticdevs.wallhaven.data.model.Data
import com.enigmaticdevs.wallhaven.data.model.Params
import com.enigmaticdevs.wallhaven.domain.repository.MainRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.HttpException
import kotlin.coroutines.coroutineContext

class PagingData(val respository : MainRepository,val params : Params) : PagingSource<Int, Data>() {
    override fun getRefreshKey(state: PagingState<Int, Data>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Data> {
      return  try {
            // Start refresh at page 1 if undefined.
            val currentPage = params.key ?: 1
            val response = respository.getWallpaperBySort(this.params,currentPage)
          if(response!=null) {
              val data = response.data
              val responseData = mutableListOf<Data>()
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