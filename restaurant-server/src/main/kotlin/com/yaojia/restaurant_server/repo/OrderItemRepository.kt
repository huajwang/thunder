package com.yaojia.restaurant_server.repo

import com.yaojia.restaurant_server.data.OrderItem
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderItemRepository : CoroutineCrudRepository<OrderItem, Long> {
    fun findByOrderId(orderId: Long): Flow<OrderItem>
}
