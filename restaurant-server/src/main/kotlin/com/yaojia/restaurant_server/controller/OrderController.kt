package com.yaojia.restaurant_server.controller

import com.yaojia.restaurant_server.config.Constants
import com.yaojia.restaurant_server.data.Category
import com.yaojia.restaurant_server.data.MenuItem
import com.yaojia.restaurant_server.data.Order
import com.yaojia.restaurant_server.data.OrderItem
import com.yaojia.restaurant_server.data.OrderStatus
import com.yaojia.restaurant_server.dto.OrderDetailsDto
import com.yaojia.restaurant_server.dto.OrderItemDto
import com.yaojia.restaurant_server.dto.OrderRequest
import com.yaojia.restaurant_server.dto.OrderResponse
import com.yaojia.restaurant_server.repo.CategoryRepository
import com.yaojia.restaurant_server.repo.CustomerRepository
import com.yaojia.restaurant_server.repo.MenuItemRepository
import com.yaojia.restaurant_server.repo.OrderItemRepository
import com.yaojia.restaurant_server.repo.OrderRepository
import com.yaojia.restaurant_server.repo.RestaurantVipConfigRepository
import com.yaojia.restaurant_server.service.OrderEvent
import com.yaojia.restaurant_server.service.OrderEventService
import com.yaojia.restaurant_server.service.OrderEventType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
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
    private val categoryRepository: CategoryRepository,
    private val restaurantVipConfigRepository: RestaurantVipConfigRepository,
    private val orderEventService: OrderEventService,
    private val customerRepository: CustomerRepository
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
                deliveryAddress = order.deliveryAddress,
                phoneNumber = order.phoneNumber,
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
        val menuItemIds = request.items.map { it.menuItemId }.filter { it > 0 }
        val menuItems = menuItemRepository.findAllById(menuItemIds).toList()
            .associateBy { it.id }

        // 2. Calculate total and validate items
        var subTotal = BigDecimal.ZERO
        val orderItemsToSave = request.items.map { itemRequest ->
            if (itemRequest.menuItemId == -999L) {
                // Handle VIP Membership special item
                val vipItem = getOrCreateVipItem(request.restaurantId)
                val price = vipItem.price
                subTotal = subTotal.add(price.multiply(BigDecimal(itemRequest.quantity)))
                OrderItem(
                    orderId = 0, // Will be updated after order save
                    menuItemId = vipItem.id!!,
                    quantity = itemRequest.quantity,
                    priceAtOrder = price
                )
            } else {
                val menuItem = menuItems[itemRequest.menuItemId]
                    ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Menu item not found: ${itemRequest.menuItemId}")
                
                val price = menuItem.price
                subTotal = subTotal.add(price.multiply(BigDecimal(itemRequest.quantity)))
                
                OrderItem(
                    orderId = 0, // Will be updated after order save
                    menuItemId = menuItem.id!!,
                    quantity = itemRequest.quantity,
                    priceAtOrder = price
                )
            }
        }

        // Calculate discount and tax
        var discount = BigDecimal.ZERO
        var isMember = false
        var hasVipItem = false

        if (request.customerId != null) {
            val customer = customerRepository.findById(request.customerId)
            if (customer != null) {
                isMember = customer.isMember
            }
        }

        // Check if order contains VIP Membership item
        // We check both the special ID -999 and the resolved menu item name
        hasVipItem = request.items.any { it.menuItemId == -999L } || 
                     menuItems.values.any { it.name == "VIP Membership" }

        if (isMember || hasVipItem) {
            val vipConfig = restaurantVipConfigRepository.findByRestaurantId(request.restaurantId)
            if (vipConfig != null && vipConfig.isEnabled) {
                val discountRate = BigDecimal.valueOf(vipConfig.discountRate)
                discount = subTotal.multiply(discountRate)
            }
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
                deliveryAddress = request.deliveryAddress,
                phoneNumber = request.phoneNumber,
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

        // 5. Upgrade customer to VIP if applicable
        if (hasVipItem && !isMember && request.customerId != null) {
            val customer = customerRepository.findById(request.customerId)
            if (customer != null && !customer.isMember) {
                customerRepository.save(customer.copy(isMember = true))
            }
        }

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

    private suspend fun getOrCreateVipItem(restaurantId: Long): MenuItem {
        // Check if VIP is enabled for this restaurant
        val vipConfig = restaurantVipConfigRepository.findByRestaurantId(restaurantId)
        if (vipConfig == null || !vipConfig.isEnabled) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "VIP Membership is not available for this restaurant")
        }

        // Try to find existing VIP item
        val existing = menuItemRepository.findByRestaurantId(restaurantId)
            .toList()
            .find { it.name == "VIP Membership" }
        
        if (existing != null) {
            // Update price if it changed in config
            if (existing.price.compareTo(vipConfig.price) != 0) {
                return menuItemRepository.save(existing.copy(price = vipConfig.price))
            }
            return existing
        }

        // Create category if needed
        val category = categoryRepository.findByRestaurantIdOrderByDisplayOrderAsc(restaurantId)
            .toList()
            .find { it.name == "Memberships" }
            ?: categoryRepository.save(
                Category(
                    restaurantId = restaurantId,
                    name = "Memberships",
                    displayOrder = 999
                )
            )

        // Create VIP item
        return menuItemRepository.save(
            MenuItem(
                restaurantId = restaurantId,
                categoryId = category.id,
                name = "VIP Membership",
                description = vipConfig.description ?: "Annual VIP Membership with exclusive benefits",
                price = vipConfig.price,
                imageUrl = vipConfig.imageUrl ?: "https://images.unsplash.com/photo-1568602471122-7832951cc4c5?ixlib=rb-4.0.3&auto=format&fit=crop&w=1470&q=80",
                isAvailable = true
            )
        )
    }
}
