package com.yaojia.snowball.data.model

data class Table(
    val id: Long,
    val restaurantId: Long,
    val tableNumber: Int,
    val qrCodeSlug: String?,
    val createdAt: String,
    val updatedAt: String
)
