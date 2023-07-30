package com.enigmaticdevs.wallhaven.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enigmaticdevs.wallhaven.response.Wallpaper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private  val repository: MainRepository,
   // private val dispatchers : DispatcherProvider
) : ViewModel() {
  /*    sealed class WallpaperEvent {
          class Success(val result : Data) : WallpaperEvent()
          class Error(val errorText : String) : WallpaperEvent()
          object Loading : WallpaperEvent()
          object Empty : WallpaperEvent()
      }*/
    private val _wallpaperList = MutableLiveData<Wallpaper?>()
    val wallpaperList : LiveData<Wallpaper?>  = _wallpaperList

    fun listWallpapers(sorting: String,purity : String,category : String,topRange : String,page : Int) {
       viewModelScope.launch {
           val response = repository.getWallpapers(sorting,purity,category,topRange,page)
           _wallpaperList.postValue(response)
       }
        }

}