package com.enigmaticdevs.wallhaven.data.remote

import com.enigmaticdevs.wallhaven.domain.repository.DataStoreRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class MyInterceptor @Inject constructor(
    private val dataStoreRepository: DataStoreRepository) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        //using DataStore to retrieve API key
        return runBlocking {
            val apiKey = dataStoreRepository.getKey().toString()
            if (apiKey.isNotEmpty()) {
                val authorized = original.newBuilder()
                    .addHeader("X-API-Key", apiKey)
                    .build()
                chain.proceed(authorized)
            } else {
                chain.proceed(original)
            }
        }
    }
}