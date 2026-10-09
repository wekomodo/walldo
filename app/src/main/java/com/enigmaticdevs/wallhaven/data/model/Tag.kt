package com.enigmaticdevs.wallhaven.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    val alias: String = "",
    val category: String ="",
    val category_id: Int = 0,
    val created_at: String = "",
    val id: Int = 0,
    val name: String = "",
    val purity: String = ""
) {}