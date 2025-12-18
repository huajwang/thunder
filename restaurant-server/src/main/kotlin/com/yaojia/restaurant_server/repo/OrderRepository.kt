package com.yaojia.restaurant_server.repo

import com.yaojia.restaurant_server.data.Order
import com.yaojia.restaurant_server.data.OrderItem
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderRepository : CoroutineCrudRepository<Order, Long> {
    fun findByRestaurantId(restaurantId: Long): Flow<Order>
}

@Repository
interface OrderItemRepository : CoroutineCrudRepository<OrderItem, Long> {
    fun findByOrderId(orderId: Long): Flow<OrderItem>
}
