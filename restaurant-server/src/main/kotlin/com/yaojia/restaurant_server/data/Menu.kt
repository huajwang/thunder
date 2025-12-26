package com.yaojia.restaurant_server.data

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("menus")
data class Menu(
    @Id
    val id: Long? = null,
    val restaurantId: Long,
    val name: String,
    val description: String? = null,
    val isActive: Boolean = true
)
