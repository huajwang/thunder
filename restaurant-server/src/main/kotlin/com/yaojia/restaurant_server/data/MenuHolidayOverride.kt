package com.yaojia.restaurant_server.data

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.time.LocalTime

@Table("menu_holiday_overrides")
data class MenuHolidayOverride(
    @Id
    val id: Long? = null,
    val restaurantId: Long,
    val menuId: Long,
    val overrideDate: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime
)
