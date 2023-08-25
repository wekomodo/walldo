package com.enigmaticdevs.wallhaven.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.enigmaticdevs.wallhaven.data.model.Params
import com.enigmaticdevs.wallhaven.data.model.Photo
import com.enigmaticdevs.wallhaven.data.model.Wallpaper
import com.enigmaticdevs.wallhaven.domain.PagingData
import com.enigmaticdevs.wallhaven.domain.repository.MainRepository
import com.enigmaticdevs.wallhaven.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private  val repository: MainRepository,
   // private val dispatchers : DispatcherProvider
) : ViewModel() {

    fun popularList(params: Params) = Pager(PagingConfig(pageSize = 1)){
        PagingData(repository, params)
    }.flow.cachedIn(viewModelScope)

    fun recentList(params: Params) = Pager(PagingConfig(pageSize = 1)){
        PagingData(repository, params)
    }.flow.cachedIn(viewModelScope)

    private val _wallpaperSearchList = MutableStateFlow<Resource<Wallpaper?>>(Resource.Loading())
    val wallpaperSearchList  = _wallpaperSearchList.asStateFlow()

    private val _wallpaper = MutableStateFlow<Resource<Photo?>>(Resource.Loading())
    val wallpaper  = _wallpaper.asStateFlow()

    fun getSearchWallpapers(
        query : String,
        params: Params,
        page : Int) {
        viewModelScope.launch {
            val response = repository.getSearchWallpapers(query,params,page)
            if(response!=null)
                _wallpaperSearchList.value = Resource.Success(response)
            else
                _wallpaperSearchList.value = Resource.Error("Failed")
        }
    }

    fun getWallpaper(id : String){
        viewModelScope.launch {
            val response = repository.getWallpaper(id)
            if(response!=null)
                _wallpaper.value = Resource.Success(response)
            else
                _wallpaper.value = Resource.Error("Failed")
        }
    }
}