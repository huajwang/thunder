package com.yaojia.snowball.data.model

data class Order(
    val id: Long,
    val restaurantId: Long,
    val tableId: Long?,
    val customerId: Long?,
    val status: String,
    val subTotal: Double,
    val tax: Double,
    val discount: Double,
    val totalAmount: Double,
    val createdAt: String,
    val updatedAt: String,
    val items: List<OrderItem>
)

data class OrderItem(
    val menuItemId: Long,
    val menuItemName: String,
    val quantity: Int,
    val price: Double
)
