package com.yaojia.restaurant_server.controller

import com.yaojia.restaurant_server.repo.UserRepository
import com.yaojia.restaurant_server.security.JwtUtil
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

data class AuthRequest(val username: String, val password: String)
data class RefreshTokenRequest(val refreshToken: String)
data class AuthResponse(val token: String, val refreshToken: String, val restaurantId: Long, val role: String)

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userRepository: UserRepository,
    private val jwtUtil: JwtUtil
) {
    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    @PostMapping("/login")
    suspend fun login(@RequestBody request: AuthRequest): AuthResponse {
        logger.info("Login attempt for user: ${request.username}")
        val user = userRepository.findByUsername(request.username)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")

        if (user.password != request.password) { // In real app, use password encoder
            logger.warn("Invalid password for user: ${request.username}")
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")
        }

        val token = jwtUtil.generateToken(user.username, user.restaurantId, user.role)
        val refreshToken = jwtUtil.generateRefreshToken(user.username)
        logger.info("Login successful for user: ${request.username}")
        return AuthResponse(token, refreshToken, user.restaurantId, user.role)
    }

    @PostMapping("/refresh")
    suspend fun refresh(@RequestBody request: RefreshTokenRequest): AuthResponse {
        logger.info("Refresh token attempt")
        if (jwtUtil.validateToken(request.refreshToken)) {
            val username = jwtUtil.getUsernameFromToken(request.refreshToken)
            logger.info("Refresh token valid for user: $username")
            val user = userRepository.findByUsername(username)
                ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")

            val newToken = jwtUtil.generateToken(user.username, user.restaurantId, user.role)
            // We can rotate the refresh token here if we want, for now we keep it
            return AuthResponse(newToken, request.refreshToken, user.restaurantId, user.role)
        } else {
            logger.warn("Invalid refresh token provided")
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token")
        }
    }
}
