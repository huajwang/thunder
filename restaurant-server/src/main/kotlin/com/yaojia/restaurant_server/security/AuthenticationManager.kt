package com.yaojia.restaurant_server.security

import kotlinx.coroutines.reactor.mono
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

import org.slf4j.LoggerFactory

@Component
class AuthenticationManager(private val jwtUtil: JwtUtil) : ReactiveAuthenticationManager {
    private val logger = LoggerFactory.getLogger(AuthenticationManager::class.java)

    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        val authToken = authentication.credentials.toString()
        return mono {
            try {
                if (jwtUtil.validateToken(authToken)) {
                    val username = jwtUtil.getUsernameFromToken(authToken)
                    val role = jwtUtil.getRoleFromToken(authToken)
                    val restaurantId = jwtUtil.getRestaurantIdFromToken(authToken)
                    
                    val authorities = listOf(SimpleGrantedAuthority("ROLE_$role"))
                    
                    val auth = UsernamePasswordAuthenticationToken(
                        username,
                        authToken,
                        authorities
                    )
                    // Store restaurantId in details for later access
                    auth.details = mapOf("restaurantId" to restaurantId)
                    auth
                } else {
                    logger.warn("Token validation failed")
                    null
                }
            } catch (e: Exception) {
                logger.error("Error during authentication", e)
                null
            }
        }
    }
}
