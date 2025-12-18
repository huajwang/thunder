package com.yaojia.restaurant_server.data

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("restaurant_tables")
data class RestaurantTable(
    @Id
    val id: Long? = null,
    val restaurantId: Long,
    val tableNumber: Int,
    val qrCodeSlug: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
