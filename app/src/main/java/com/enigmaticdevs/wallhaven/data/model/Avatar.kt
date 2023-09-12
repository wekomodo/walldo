package com.enigmaticdevs.wallhaven.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Avatar(
    val `128px`: String,
    val `200px`: String,
    val `20px`: String,
    val `32px`: String
) : java.io.Serializable