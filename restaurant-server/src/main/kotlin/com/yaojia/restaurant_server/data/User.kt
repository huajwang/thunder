package com.yaojia.restaurant_server.data

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("users")
data class User(
    @Id
    val id: Long? = null,
    val restaurantId: Long,
    val username: String,
    val password: String, // In a real app, this should be hashed
    val role: String, // e.g., "ADMIN", "STAFF"
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
