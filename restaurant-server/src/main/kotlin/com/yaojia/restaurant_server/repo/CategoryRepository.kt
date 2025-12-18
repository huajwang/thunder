package com.yaojia.restaurant_server.repo

import com.yaojia.restaurant_server.data.Category
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : CoroutineCrudRepository<Category, Long> {
    fun findByRestaurantIdOrderByDisplayOrderAsc(restaurantId: Long): Flow<Category>
}
