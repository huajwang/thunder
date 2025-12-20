package com.yaojia.restaurant_server.data

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("restaurants")
data class Restaurant(
    @Id
    val id: Long? = null,
    val name: String,
    val slug: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val address: String? = null,
    val phoneNumber: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
