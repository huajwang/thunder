package com.yaojia.restaurant_server.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtUtil {
    // In production, use a secure key from configuration
    private val secret = "ThisIsASecretKeyForJwtTokenGenerationAndValidation1234567890"
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())
    private val expirationTime = 86400000L // 1 day

    fun generateToken(username: String, restaurantId: Long, role: String): String {
        return Jwts.builder()
            .subject(username)
            .claim("restaurantId", restaurantId)
            .claim("role", role)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expirationTime))
            .signWith(key)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: Exception) {
            // Log the exception for debugging
            println("Token validation error: ${e.message}")
            false
        }
    }

    fun getUsernameFromToken(token: String): String {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject
    }

    fun getRestaurantIdFromToken(token: String): Long {
        val claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
        
        // Handle potential type mismatch (Integer vs Long)
        val id = claims["restaurantId"]
        return if (id is Int) id.toLong() else id as Long
    }
    
    fun getRoleFromToken(token: String): String {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
            .get("role", String::class.java)
    }
}
