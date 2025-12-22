package com.yaojia.snowball.data.model

data class Restaurant(
    val id: Long,
    val name: String,
    val slug: String,
    val description: String?,
    val imageUrl: String?,
    val address: String?,
    val phoneNumber: String?
)
