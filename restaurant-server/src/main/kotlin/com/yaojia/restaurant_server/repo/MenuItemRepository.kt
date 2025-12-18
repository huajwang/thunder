package com.yaojia.restaurant_server.repo

import com.yaojia.restaurant_server.data.MenuItem
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MenuItemRepository : CoroutineCrudRepository<MenuItem, Long> {
    fun findByRestaurantId(restaurantId: Long): Flow<MenuItem>
    fun findByRestaurantIdAndCategoryId(restaurantId: Long, categoryId: Long): Flow<MenuItem>
}
