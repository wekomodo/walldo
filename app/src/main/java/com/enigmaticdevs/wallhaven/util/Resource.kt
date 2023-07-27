package com.enigmaticdevs.wallhaven.util

sealed class Resource<T>(val data : T?, val message : String?) {
    class  Success<T>(data: T) : Resource<T>(data,null)
    class  Error<T>(message: String, data : T? = null) : Resource<T>(data,message)
    class Loading<T>(data : T? = null) : Resource<T>(data,null)
}