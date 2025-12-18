package com.yaojia.restaurant_server.repo

import com.yaojia.restaurant_server.data.Order
import com.yaojia.restaurant_server.data.OrderItem
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

import com.yaojia.restaurant_server.data.OrderStatus

@Repository
interface OrderRepository : CoroutineCrudRepository<Order, Long> {
    fun findByRestaurantId(restaurantId: Long): Flow<Order>
    fun findByRestaurantIdAndStatusIn(restaurantId: Long, statuses: List<OrderStatus>): Flow<Order>
    fun findByTableIdAndStatusIn(tableId: Long, statuses: List<OrderStatus>): Flow<Order>
}
