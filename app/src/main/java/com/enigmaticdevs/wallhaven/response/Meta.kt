package com.enigmaticdevs.wallhaven.response

data class Meta(
    val current_page: Int,
    val last_page: Int,
    val per_page: Int,
    val query: Any,
    val seed: Any,
    val total: Int
)