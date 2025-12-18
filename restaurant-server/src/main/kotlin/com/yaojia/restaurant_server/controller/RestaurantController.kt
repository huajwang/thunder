package com.yaojia.restaurant_server.controller

import com.yaojia.restaurant_server.data.Category
import com.yaojia.restaurant_server.data.MenuItem
import com.yaojia.restaurant_server.data.Restaurant
import com.yaojia.restaurant_server.repo.CategoryRepository
import com.yaojia.restaurant_server.repo.MenuItemRepository
import com.yaojia.restaurant_server.repo.RestaurantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/restaurants")
class RestaurantController(
    private val restaurantRepository: RestaurantRepository,
    private val categoryRepository: CategoryRepository,
    private val menuItemRepository: MenuItemRepository
) {

    @GetMapping("/slug/{slug}")
    suspend fun getRestaurantBySlug(@PathVariable slug: String): Restaurant {
        return restaurantRepository.findBySlug(slug).firstOrNull()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found")
    }

    @GetMapping("/{id}/categories")
    fun getCategories(@PathVariable id: Long): Flow<Category> {
        return categoryRepository.findByRestaurantIdOrderByDisplayOrderAsc(id)
    }

    @GetMapping("/{id}/menu-items")
    fun getMenuItems(@PathVariable id: Long): Flow<MenuItem> {
        return menuItemRepository.findByRestaurantId(id)
    }
}
