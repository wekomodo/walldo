package com.enigmaticdevs.wallhaven.util


sealed class Resource<T>(val status : Status, val data : T?, val message : String?) {
    class  Success<T>(data: T) : Resource<T>(Status.SUCCESS,data,null)
    class  Error<T>(message: String, data : T? = null) : Resource<T>(Status.ERROR,null,message)
    class Loading<T>(data : T? = null) : Resource<T>(Status.LOADING,data,null)
}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}