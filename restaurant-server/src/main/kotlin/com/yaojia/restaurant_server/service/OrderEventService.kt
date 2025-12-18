package com.yaojia.restaurant_server.service

import com.yaojia.restaurant_server.data.OrderStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import org.springframework.stereotype.Service

data class OrderEvent(
    val orderId: Long,
    val restaurantId: Long,
    val status: OrderStatus,
    val type: OrderEventType
)

enum class OrderEventType {
    CREATED, UPDATED
}

@Service
class OrderEventService {
    private val _events = MutableSharedFlow<OrderEvent>(extraBufferCapacity = 100)
    val events = _events.asSharedFlow()

    suspend fun emit(event: OrderEvent) {
        _events.emit(event)
    }

    fun subscribe(restaurantId: Long): Flow<OrderEvent> {
        return events.filter { it.restaurantId == restaurantId }
    }
}
