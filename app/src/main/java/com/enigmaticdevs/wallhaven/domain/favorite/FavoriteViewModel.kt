package com.enigmaticdevs.wallhaven.domain.favorite

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enigmaticdevs.wallhaven.data.favorite.models.FavoriteImages
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(private var repository: FavoriteRepository) : ViewModel() {
    val readAllData : LiveData<MutableList<FavoriteImages>> = repository.readAllData()
    fun addImage(favoriteImage: FavoriteImages){
        viewModelScope.launch(Dispatchers.IO){
            repository.addImage(favoriteImage)
        }
    }
    fun deleteImage(favoriteImage: FavoriteImages)
    {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteImage(favoriteImage)
        }
    }
    fun deleteAll()
    {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
        }
    }

    fun getOnlyUrls()
    {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getOnlyUrls()
        }
    }
}