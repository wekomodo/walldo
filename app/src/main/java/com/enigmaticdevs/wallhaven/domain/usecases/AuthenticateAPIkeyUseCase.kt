package com.enigmaticdevs.wallhaven.domain.usecases

import com.enigmaticdevs.wallhaven.data.model.AuthenticateAPIkey
import com.enigmaticdevs.wallhaven.domain.repository.MainRepository
import com.enigmaticdevs.wallhaven.util.DispatcherProvider
import com.enigmaticdevs.wallhaven.util.Resource
import javax.inject.Inject

class AuthenticateAPIkeyUseCase @Inject constructor(
    private val repository: MainRepository,
    private val dispatcherProvider: DispatcherProvider
)  {

    suspend operator fun invoke(key : String) : Resource<AuthenticateAPIkey?>{
        val result = repository.authenticateAPIkey(key)
        result?.let{
            return  Resource.Success(result)
        } ?: run{
            return Resource.Error("Invalid API key")
        }

    }
}