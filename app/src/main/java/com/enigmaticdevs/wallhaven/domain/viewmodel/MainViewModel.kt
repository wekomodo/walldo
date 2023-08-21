package com.enigmaticdevs.wallhaven.domain.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enigmaticdevs.wallhaven.data.model.Data
import com.enigmaticdevs.wallhaven.data.model.Params
import com.enigmaticdevs.wallhaven.data.model.Wallpaper
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
    private val _wallpaperListPopular = MutableStateFlow<Resource<Wallpaper?>>(Resource.Loading())
    val wallpaperListPopular   = _wallpaperListPopular.asStateFlow()

    private val _wallpaperListRecent = MutableStateFlow<Resource<Wallpaper?>>(Resource.Loading())
    val wallpaperListRecent   = _wallpaperListRecent.asStateFlow()

    private val _wallpaperSearchList = MutableStateFlow<Resource<Wallpaper?>>(Resource.Loading())
    val wallpaperSearchList  = _wallpaperSearchList.asStateFlow()

    private val _wallpaper = MutableLiveData<Data?>()
    val wallpaper : LiveData<Data?>  = _wallpaper

    fun getWallpaperPopular(params : Params,
                           page : Int) {
       viewModelScope.launch {
            val response = repository.getWallpaperBySort(params,page)
           if(response!=null)
               _wallpaperListPopular.value = Resource.Success(response)
           else
               _wallpaperListPopular.value = Resource.Error("Failed")
       }
        }
    fun getWallpaperRecent(params : Params,
                            page : Int) {
        viewModelScope.launch {
            val response = repository.getWallpaperBySort(params,page)
            if(response!=null)
                _wallpaperListRecent.value = Resource.Success(response)
            else
                _wallpaperListRecent.value = Resource.Error("Failed")
        }
    }
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
            _wallpaper.value = response
        }
    }
}