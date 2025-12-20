package com.yaojia.restaurant_server.dto

data class OrderRequest(
    val restaurantId: Long,
    val tableId: Long?,
    val customerId: Long? = null,
    val deliveryAddress: String? = null,
    val items: List<OrderItemRequest>
)

data class OrderItemRequest(
    val menuItemId: Long,
    val quantity: Int
)

data class OrderResponse(
    val id: Long,
    val status: String,
    val totalAmount: java.math.BigDecimal
)
