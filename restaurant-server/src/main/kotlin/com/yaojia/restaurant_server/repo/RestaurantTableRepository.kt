package com.yaojia.restaurant_server.repo

import com.yaojia.restaurant_server.data.RestaurantTable
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RestaurantTableRepository : CoroutineCrudRepository<RestaurantTable, Long> {
    fun findByRestaurantId(restaurantId: Long): Flow<RestaurantTable>
    fun findByRestaurantIdAndTableNumber(restaurantId: Long, tableNumber: Int): Flow<RestaurantTable>
}
