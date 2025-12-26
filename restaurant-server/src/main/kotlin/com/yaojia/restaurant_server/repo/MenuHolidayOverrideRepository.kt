package com.yaojia.restaurant_server.repo

import com.yaojia.restaurant_server.data.MenuHolidayOverride
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface MenuHolidayOverrideRepository : CoroutineCrudRepository<MenuHolidayOverride, Long> {
    fun findByRestaurantIdAndOverrideDate(restaurantId: Long, overrideDate: LocalDate): Flow<MenuHolidayOverride>
}
