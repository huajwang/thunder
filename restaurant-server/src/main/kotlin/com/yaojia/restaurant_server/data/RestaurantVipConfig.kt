package com.yaojia.restaurant_server.data

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("restaurant_vip_configs")
data class RestaurantVipConfig(
    @Id
    val id: Long? = null,
    val restaurantId: Long,
    val isEnabled: Boolean = false,
    val price: BigDecimal,
    val description: String? = null,
    val imageUrl: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
