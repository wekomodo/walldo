package com.enigmaticdevs.wallhaven.domain.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enigmaticdevs.wallhaven.data.model.Data
import com.enigmaticdevs.wallhaven.data.model.Wallpaper
import com.enigmaticdevs.wallhaven.domain.repository.MainRepository
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
      /*sealed class WallpaperEvent(val wallpaper : Wallpaper) {
          class Success(val result : Wallpaper) : WallpaperEvent()
          class Error(val errorText : String) : WallpaperEvent()
          object Loading : WallpaperEvent()
          class Empty(val empty : Wallpaper) : WallpaperEvent()
      }*/
    private val _wallpaperList = MutableStateFlow<Wallpaper?>(null)
    val wallpaperList   = _wallpaperList.asStateFlow()

    private val _wallpaperSearchList = MutableLiveData<Wallpaper?>()
    val wallpaperSearchList : LiveData<Wallpaper?>  = _wallpaperSearchList

    private val _wallpaper = MutableLiveData<Data?>()
    val wallpaper : LiveData<Data?>  = _wallpaper

    fun getWallpaperBySort(sorting: String,
                           purity : String,
                           category : String,
                           topRange : String,
                           ratio : String,
                           resolution : String,
                           page : Int) {
       viewModelScope.launch {
           val response = repository.getWallpaperBySort(sorting,purity,category,topRange,ratio,resolution,page)
           _wallpaperList.value = response
       }
        }
    fun getSearchWallpapers(
        query : String,
        sorting: String,
        purity : String,
        category : String,
        topRange : String,
        ratio : String,
        resolution : String,
        page : Int) {
        viewModelScope.launch {
            val response = repository.getSearchWallpapers(query,sorting,purity,category,topRange,ratio,resolution,page)
            _wallpaperSearchList.value = response
        }
    }

    fun getWallpaper(id : String){
        viewModelScope.launch {
            val response = repository.getWallpaper(id)
            _wallpaper.value = response
        }
    }
}