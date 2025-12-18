package com.yaojia.restaurant_server.controller

import com.yaojia.restaurant_server.data.Order
import com.yaojia.restaurant_server.data.OrderItem
import com.yaojia.restaurant_server.data.OrderStatus
import com.yaojia.restaurant_server.dto.OrderRequest
import com.yaojia.restaurant_server.dto.OrderResponse
import com.yaojia.restaurant_server.repo.MenuItemRepository
import com.yaojia.restaurant_server.repo.OrderItemRepository
import com.yaojia.restaurant_server.repo.OrderRepository
import kotlinx.coroutines.flow.toList
import org.springframework.http.HttpStatus
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val menuItemRepository: MenuItemRepository
) {

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
        var totalAmount = BigDecimal.ZERO
        val orderItemsToSave = request.items.map { itemRequest ->
            val menuItem = menuItems[itemRequest.menuItemId]
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Menu item not found: ${itemRequest.menuItemId}")
            
            val lineTotal = menuItem.price.multiply(BigDecimal(itemRequest.quantity))
            totalAmount = totalAmount.add(lineTotal)

            OrderItem(
                orderId = 0, // Will be updated after saving order
                menuItemId = menuItem.id!!,
                quantity = itemRequest.quantity,
                priceAtOrder = menuItem.price
            )
        }

        // 3. Save Order
        val savedOrder = orderRepository.save(
            Order(
                restaurantId = request.restaurantId,
                tableId = request.tableId,
                totalAmount = totalAmount,
                status = OrderStatus.PENDING
            )
        )

        // 4. Save Order Items
        val itemsWithOrderId = orderItemsToSave.map { it.copy(orderId = savedOrder.id!!) }
        orderItemRepository.saveAll(itemsWithOrderId).toList()

        return OrderResponse(
            id = savedOrder.id!!,
            status = savedOrder.status.name,
            totalAmount = savedOrder.totalAmount
        )
    }
}
