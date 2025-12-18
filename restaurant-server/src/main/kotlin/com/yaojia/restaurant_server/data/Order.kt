package com.yaojia.restaurant_server.data

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("orders")
data class Order(
    @Id
    val id: Long? = null,
    val restaurantId: Long,
    val tableId: Long? = null,
    val customerId: Long? = null,
    val customerName: String? = null,
    val status: OrderStatus = OrderStatus.PENDING,
    val subTotal: BigDecimal,
    val tax: BigDecimal,
    val discount: BigDecimal,
    val totalAmount: BigDecimal,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class OrderStatus {
    PENDING, PREPARING, READY, COMPLETED, CANCELLED, PAID
}

@Table("order_items")
data class OrderItem(
    @Id
    val id: Long? = null,
    val orderId: Long,
    val menuItemId: Long,
    val quantity: Int,
    val priceAtOrder: BigDecimal
)
