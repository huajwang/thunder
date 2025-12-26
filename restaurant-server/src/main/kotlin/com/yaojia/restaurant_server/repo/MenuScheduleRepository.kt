package com.yaojia.restaurant_server.repo

import com.yaojia.restaurant_server.data.MenuSchedule
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MenuScheduleRepository : CoroutineCrudRepository<MenuSchedule, Long> {
    fun findByRestaurantId(restaurantId: Long): Flow<MenuSchedule>
}
