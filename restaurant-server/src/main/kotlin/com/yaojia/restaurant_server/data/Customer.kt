package com.yaojia.restaurant_server.data

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("customers")
data class Customer(
    @Id
    val id: Long? = null,
    val restaurantId: Long,
    val phoneNumber: String,
    val isMember: Boolean = false,
    val joinedAt: LocalDateTime = LocalDateTime.now(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
