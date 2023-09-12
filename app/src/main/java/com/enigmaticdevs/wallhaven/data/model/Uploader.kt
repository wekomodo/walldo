package com.enigmaticdevs.wallhaven.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Uploader(
    val username : String,
    val group : String,
    val avatar : Avatar
) : java.io.Serializable