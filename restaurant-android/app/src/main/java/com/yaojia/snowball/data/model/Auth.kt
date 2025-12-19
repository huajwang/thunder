package com.yaojia.snowball.data.model

data class AuthRequest(val username: String, val password: String)
data class AuthResponse(val token: String, val restaurantId: Long, val role: String)
