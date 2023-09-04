package com.enigmaticdevs.wallhaven.domain.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enigmaticdevs.wallhaven.data.model.Params
import com.enigmaticdevs.wallhaven.domain.repository.DataStoreRepository
import com.enigmaticdevs.wallhaven.util.DispatcherProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DataStoreViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    private val dispatchers : DispatcherProvider
) : ViewModel() {


    val apiKey = MutableLiveData<String>()
    val settingsMigrated = MutableLiveData<Boolean>()
    val settings = MutableLiveData<Params>()


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

    fun saveSettings(params : Params){
        viewModelScope.launch  (dispatchers.io){
            dataStoreRepository.saveSettings(params)
        }
    }

    fun readSettings(){
        viewModelScope.launch(dispatchers.io){
            dataStoreRepository.readSettings().collect{
                settings.postValue(it)
            }
        }
    }
    fun setSettingsMigrated(){
        viewModelScope.launch(dispatchers.io) {
              dataStoreRepository.setSettingsMigrated()
        }
    }
    fun getSettingsMigrated(){
        viewModelScope.launch(dispatchers.io) {
            dataStoreRepository.getSettingsMigrated().collect{
                settingsMigrated.postValue(it)
            }
        }
    }
}