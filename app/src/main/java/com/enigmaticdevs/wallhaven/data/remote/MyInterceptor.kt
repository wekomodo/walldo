package com.enigmaticdevs.wallhaven.data.remote

import okhttp3.Interceptor
import okhttp3.Response

class MyInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .addHeader("X-API-key","123")
            .build()
        return chain.proceed(request)
    }
}