package com.yaojia.restaurant_server.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class OrderDetailsDto(
    val id: Long,
    val restaurantId: Long,
    val tableId: Long?,
    val customerId: Long? = null,
    val deliveryAddress: String? = null,
    val status: String,
    val subTotal: BigDecimal,
    val tax: BigDecimal,
    val discount: BigDecimal,
    val totalAmount: BigDecimal,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val items: List<OrderItemDto>
)

data class OrderItemDto(
    val menuItemId: Long,
    val menuItemName: String,
    val quantity: Int,
    val price: BigDecimal
)
