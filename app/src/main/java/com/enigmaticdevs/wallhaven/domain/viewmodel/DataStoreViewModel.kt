package com.enigmaticdevs.wallhaven.domain.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enigmaticdevs.wallhaven.data.model.Photo
import com.enigmaticdevs.wallhaven.domain.repository.DataStoreRepository
import com.enigmaticdevs.wallhaven.util.DispatcherProvider
import com.enigmaticdevs.wallhaven.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DataStoreViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    private val dispatchers : DispatcherProvider
) : ViewModel() {


    val apiKey = MutableLiveData<String>()


    fun saveAPIkey(key : String){
            viewModelScope.launch(dispatchers.io) {
                dataStoreRepository.saveAPIkey(key)
            }
    }
    fun getAPIkey(){
        viewModelScope.launch(dispatchers.io) {
            dataStoreRepository.getAPIkey().collect{
                apiKey.postValue(it)
            }
        }
    }
}