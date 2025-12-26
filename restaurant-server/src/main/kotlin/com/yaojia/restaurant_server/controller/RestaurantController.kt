package com.yaojia.restaurant_server.controller

import com.yaojia.restaurant_server.data.Category
import com.yaojia.restaurant_server.data.MenuItem
import com.yaojia.restaurant_server.data.Restaurant
import com.yaojia.restaurant_server.data.RestaurantVipConfig
import com.yaojia.restaurant_server.repo.CategoryRepository
import com.yaojia.restaurant_server.repo.MenuItemRepository
import com.yaojia.restaurant_server.repo.MenuItemVariantRepository
import com.yaojia.restaurant_server.repo.RestaurantRepository
import com.yaojia.restaurant_server.repo.RestaurantVipConfigRepository
import com.yaojia.restaurant_server.service.MenuService
import com.yaojia.restaurant_server.dto.MenuResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/restaurants")
class RestaurantController(
    private val restaurantRepository: RestaurantRepository,
    private val categoryRepository: CategoryRepository,
    private val menuItemRepository: MenuItemRepository,
    private val menuItemVariantRepository: MenuItemVariantRepository,
    private val restaurantVipConfigRepository: RestaurantVipConfigRepository,
    private val menuService: MenuService
) {

    @GetMapping("/slug/{slug}")
    suspend fun getRestaurantBySlug(@PathVariable slug: String): Restaurant {
        return restaurantRepository.findBySlug(slug).firstOrNull()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found")
    }

    @GetMapping("/slug/{slug}/active-menu")
    suspend fun getActiveMenu(@PathVariable slug: String): MenuResponse {
        return menuService.getActiveMenu(slug)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No active menu found for current time")
    }

    @GetMapping("/{id}")
    suspend fun getRestaurantById(@PathVariable id: Long): Restaurant {
        return restaurantRepository.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found")
    }

    @GetMapping
    fun getAllRestaurants(): Flow<Restaurant> {
        return restaurantRepository.findAll()
    }

    @GetMapping("/{id}/categories")
    fun getCategories(@PathVariable id: Long): Flow<Category> {
        return categoryRepository.findByRestaurantIdOrderByDisplayOrderAsc(id)
    }

    @GetMapping("/{id}/menu-items")
    suspend fun getMenuItems(@PathVariable id: Long): List<MenuItem> {
        val items = menuItemRepository.findByRestaurantId(id).toList()
        return items.map { item ->
            val variants = menuItemVariantRepository.findByMenuItemId(item.id!!).toList()
            item.apply { this.variants = variants }
        }
    }

    @GetMapping("/{id}/menu-items/search")
    suspend fun searchMenuItems(@PathVariable id: Long, @RequestParam q: String): List<MenuItem> {
        val items = menuItemRepository.searchByRestaurantIdAndQuery(id, q).toList()
        return items.map { item ->
            val variants = menuItemVariantRepository.findByMenuItemId(item.id!!).toList()
            item.apply { this.variants = variants }
        }
    }

    @GetMapping("/{id}/vip-config")
    suspend fun getVipConfig(@PathVariable id: Long): RestaurantVipConfig {
        return restaurantVipConfigRepository.findByRestaurantId(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "VIP config not found")
    }
}
