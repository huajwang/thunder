package com.yaojia.restaurant_server.controller

import com.yaojia.restaurant_server.data.OrderStatus
import com.yaojia.restaurant_server.data.RestaurantTable
import com.yaojia.restaurant_server.dto.OrderDetailsDto
import com.yaojia.restaurant_server.dto.OrderItemDto
import com.yaojia.restaurant_server.repo.MenuItemRepository
import com.yaojia.restaurant_server.repo.OrderItemRepository
import com.yaojia.restaurant_server.repo.OrderRepository
import com.yaojia.restaurant_server.repo.RestaurantTableRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal

@RestController
@RequestMapping("/api/tables")
class TableController(
    private val restaurantTableRepository: RestaurantTableRepository,
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val menuItemRepository: MenuItemRepository
) {

    @GetMapping
    fun getTables(@RequestParam restaurantId: Long, authentication: Authentication): Flow<RestaurantTable> {
        val details = authentication.details as? Map<*, *>
        val userRestaurantId = details?.get("restaurantId") as? Long
        if (userRestaurantId != null && userRestaurantId != restaurantId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied")
        }
        return restaurantTableRepository.findByRestaurantId(restaurantId)
    }

    @GetMapping("/{tableId}/bill")
    suspend fun getTableBill(@PathVariable tableId: Long, authentication: Authentication): List<OrderDetailsDto> {
        val table = restaurantTableRepository.findById(tableId) 
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Table not found")
            
        val details = authentication.details as? Map<*, *>
        val userRestaurantId = details?.get("restaurantId") as? Long
        if (userRestaurantId != null && userRestaurantId != table.restaurantId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied")
        }

        // Fetch all orders for the table that are NOT PAID and NOT CANCELLED
        val activeStatuses = listOf(OrderStatus.PENDING, OrderStatus.PREPARING, OrderStatus.READY, OrderStatus.COMPLETED)
        val orders = orderRepository.findByTableIdAndStatusIn(tableId, activeStatuses).toList()

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
                status = order.status.name,
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

    @PostMapping("/{tableId}/checkout")
    @Transactional
    suspend fun checkoutTable(@PathVariable tableId: Long, authentication: Authentication) {
        val table = restaurantTableRepository.findById(tableId) 
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Table not found")
            
        val details = authentication.details as? Map<*, *>
        val userRestaurantId = details?.get("restaurantId") as? Long
        if (userRestaurantId != null && userRestaurantId != table.restaurantId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied")
        }

        val activeStatuses = listOf(OrderStatus.PENDING, OrderStatus.PREPARING, OrderStatus.READY, OrderStatus.COMPLETED)
        val orders = orderRepository.findByTableIdAndStatusIn(tableId, activeStatuses).toList()

        if (orders.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "No active orders to checkout")
        }

        val paidOrders = orders.map { it.copy(status = OrderStatus.PAID) }
        orderRepository.saveAll(paidOrders).toList()
    }
}
