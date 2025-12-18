package com.yaojia.restaurant_server.repo

import com.yaojia.restaurant_server.data.Restaurant
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RestaurantRepository : CoroutineCrudRepository<Restaurant, Long> {
    fun findBySlug(slug: String): Flow<Restaurant>
}
