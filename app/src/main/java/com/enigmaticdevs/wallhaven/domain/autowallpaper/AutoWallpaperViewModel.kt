package com.enigmaticdevs.wallhaven.autoWallpaperdb

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enigmaticdevs.wallhaven.data.autowallpaper.models.AutoWallpaper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AutoWallpaperViewModel @Inject constructor(private var repository: AutoWallpaperRepository) : ViewModel() {
    val readAllData : LiveData<MutableList<AutoWallpaper>> = repository.readAllData()
    fun addImage(autoWallpaper: AutoWallpaper){
        viewModelScope.launch(Dispatchers.IO){
            repository.addImage(autoWallpaper)
        }
    }
    fun deleteImage(autoWallpaper: AutoWallpaper)
    {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteImage(autoWallpaper)
        }
    }
    fun deleteAll()
    {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
        }
    }
}