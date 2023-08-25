package com.enigmaticdevs.wallhaven.data.model

data class Tag(
    val alias: String,
    val category: String,
    val category_id: Int,
    val created_at: String,
    val id: Int,
    val name: String,
    val purity: String
)