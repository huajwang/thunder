package com.yaojia.restaurant_server.repo

import com.yaojia.restaurant_server.data.MenuItem
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

import org.springframework.data.r2dbc.repository.Query

@Repository
interface MenuItemRepository : CoroutineCrudRepository<MenuItem, Long> {
    fun findByRestaurantId(restaurantId: Long): Flow<MenuItem>
    fun findByRestaurantIdAndCategoryId(restaurantId: Long, categoryId: Long): Flow<MenuItem>

    @Query("SELECT * FROM menu_items WHERE restaurant_id = :restaurantId AND (LOWER(name) LIKE CONCAT('%', LOWER(:query), '%') OR LOWER(description) LIKE CONCAT('%', LOWER(:query), '%'))")
    fun searchByRestaurantIdAndQuery(restaurantId: Long, query: String): Flow<MenuItem>
}
