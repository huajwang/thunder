package com.yaojia.restaurant_server.data

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("menu_items")
data class MenuItem(
    @Id
    val id: Long? = null,
    val restaurantId: Long,
    val categoryId: Long? = null,
    val name: String,
    val description: String? = null,
    val price: BigDecimal,
    val imageUrl: String? = null,
    val isAvailable: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
