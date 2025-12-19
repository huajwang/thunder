package com.yaojia.restaurant_server.repo

import com.yaojia.restaurant_server.data.RestaurantVipConfig
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface RestaurantVipConfigRepository : CoroutineCrudRepository<RestaurantVipConfig, Long> {
    suspend fun findByRestaurantId(restaurantId: Long): RestaurantVipConfig?
}
