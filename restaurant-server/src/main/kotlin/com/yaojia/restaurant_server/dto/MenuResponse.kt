package com.yaojia.restaurant_server.dto

import com.yaojia.restaurant_server.data.MenuItem

data class CategoryWithItems(
    val id: Long,
    val name: String,
    val displayOrder: Int,
    val items: List<MenuItem>
)

data class MenuResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val categories: List<CategoryWithItems>
)
