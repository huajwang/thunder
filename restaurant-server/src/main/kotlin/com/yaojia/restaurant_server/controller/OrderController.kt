package com.yaojia.restaurant_server.controller

import com.yaojia.restaurant_server.config.Constants
import com.yaojia.restaurant_server.data.Order
import com.yaojia.restaurant_server.data.OrderItem
import com.yaojia.restaurant_server.data.OrderStatus
import com.yaojia.restaurant_server.dto.OrderDetailsDto
import com.yaojia.restaurant_server.dto.OrderItemDto
import com.yaojia.restaurant_server.dto.OrderRequest
import com.yaojia.restaurant_server.dto.OrderResponse
import com.yaojia.restaurant_server.repo.MenuItemRepository
import com.yaojia.restaurant_server.repo.OrderItemRepository
import com.yaojia.restaurant_server.repo.OrderRepository
import com.yaojia.restaurant_server.service.OrderEvent
import com.yaojia.restaurant_server.service.OrderEventService
import com.yaojia.restaurant_server.service.OrderEventType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val menuItemRepository: MenuItemRepository,
    private val orderEventService: OrderEventService
) {

    @GetMapping(value = ["/stream"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamOrders(@RequestParam restaurantId: Long, authentication: Authentication): Flow<OrderEvent> {
        val details = authentication.details as? Map<*, *>
        val userRestaurantId = details?.get("restaurantId") as? Long
        
        if (userRestaurantId != null && userRestaurantId != restaurantId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied")
        }
        
        return orderEventService.subscribe(restaurantId)
    }

    @GetMapping
    suspend fun getOrders(
        @RequestParam restaurantId: Long,
        @RequestParam(required = false) statuses: List<OrderStatus>?,
        authentication: Authentication
    ): List<OrderDetailsDto> {
        val details = authentication.details as? Map<*, *>
        val userRestaurantId = details?.get("restaurantId") as? Long
        
        if (userRestaurantId != null && userRestaurantId != restaurantId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied")
        }

        val orders = if (statuses != null && statuses.isNotEmpty()) {
            orderRepository.findByRestaurantIdAndStatusIn(restaurantId, statuses).toList()
        } else {
            orderRepository.findByRestaurantId(restaurantId).toList()
        }

        if (orders.isEmpty()) return emptyList()

        val orderIds = orders.mapNotNull { it.id }
        val orderItems = orderItemRepository.findByOrderIdIn(orderIds).toList()

        val menuItemIds = orderItems.map { it.menuItemId }.distinct()
        val menuItems = menuItemRepository.findAllById(menuItemIds).toList().associateBy { it.id }

        return orders.map { order ->
            val itemsForOrder = orderItems.filter { it.orderId == order.id }
            OrderDetailsDto(
                id = order.id!!,
                restaurantId = order.restaurantId,
                tableId = order.tableId,
                customerId = order.customerId,
                status = order.status.name,
                subTotal = order.subTotal,
                tax = order.tax,
                discount = order.discount,
                totalAmount = order.totalAmount,
                createdAt = order.createdAt,
                updatedAt = order.updatedAt,
                items = itemsForOrder.map { item ->
                    val menuItem = menuItems[item.menuItemId]
                    OrderItemDto(
                        menuItemId = item.menuItemId,
                        menuItemName = menuItem?.name ?: "Unknown",
                        quantity = item.quantity,
                        price = item.priceAtOrder
                    )
                }
            )
        }
    }

    @PutMapping("/{id}/status")
    suspend fun updateOrderStatus(
        @PathVariable id: Long,
        @RequestBody newStatus: Map<String, String>
    ): OrderResponse {
        val statusStr = newStatus["status"] ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Status required")
        val status = try {
            OrderStatus.valueOf(statusStr)
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status")
        }

        val order = orderRepository.findById(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found")
        val updatedOrder = orderRepository.save(order.copy(status = status))

        orderEventService.emit(
            OrderEvent(
                orderId = updatedOrder.id!!,
                restaurantId = updatedOrder.restaurantId,
                status = updatedOrder.status,
                type = OrderEventType.UPDATED
            )
        )

        return OrderResponse(
            id = updatedOrder.id!!,
            status = updatedOrder.status.name,
            totalAmount = updatedOrder.totalAmount
        )
    }

    @PostMapping
    @Transactional
    suspend fun placeOrder(@RequestBody request: OrderRequest): OrderResponse {
        if (request.items.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Order must contain items")
        }

        // 1. Fetch all menu items to get current prices
        val menuItemIds = request.items.map { it.menuItemId }
        val menuItems = menuItemRepository.findAllById(menuItemIds).toList()
            .associateBy { it.id }

        // 2. Calculate total and validate items
        var subTotal = BigDecimal.ZERO
        val orderItemsToSave = request.items.map { itemRequest ->
            val menuItem = menuItems[itemRequest.menuItemId]
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Menu item not found: ${itemRequest.menuItemId}")
            
            val lineTotal = menuItem.price.multiply(BigDecimal(itemRequest.quantity))
            subTotal = subTotal.add(lineTotal)

            OrderItem(
                orderId = 0, // Will be updated after saving order
                menuItemId = menuItem.id!!,
                quantity = itemRequest.quantity,
                priceAtOrder = menuItem.price
            )
        }

        // Calculate discount and tax
        val discount = if (request.customerId != null) {
            // Simple check: if customerId is present, apply 10% discount.
            // In a real app, we should verify membership status from DB.
            subTotal.multiply(BigDecimal("0.10"))
        } else {
            BigDecimal.ZERO
        }

        val taxableAmount = subTotal.subtract(discount)
        val tax = taxableAmount.multiply(Constants.TAX_RATE)
        val totalAmount = taxableAmount.add(tax)

        // 3. Save Order
        val savedOrder = orderRepository.save(
            Order(
                restaurantId = request.restaurantId,
                tableId = request.tableId,
                customerId = request.customerId,
                subTotal = subTotal,
                tax = tax,
                discount = discount,
                totalAmount = totalAmount,
                status = OrderStatus.PENDING
            )
        )

        // 4. Save Order Items
        val itemsWithOrderId = orderItemsToSave.map { it.copy(orderId = savedOrder.id!!) }
        orderItemRepository.saveAll(itemsWithOrderId).toList()

        orderEventService.emit(
            OrderEvent(
                orderId = savedOrder.id!!,
                restaurantId = savedOrder.restaurantId,
                status = savedOrder.status,
                type = OrderEventType.CREATED
            )
        )

        return OrderResponse(
            id = savedOrder.id!!,
            status = savedOrder.status.name,
            totalAmount = savedOrder.totalAmount
        )
    }
}
