package com.yaojia.restaurant_server.data

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalTime

@Table("menu_schedules")
data class MenuSchedule(
    @Id
    val id: Long? = null,
    val restaurantId: Long,
    val menuId: Long,
    val dayOfWeek: String,
    val startTime: LocalTime,
    val endTime: LocalTime
)
