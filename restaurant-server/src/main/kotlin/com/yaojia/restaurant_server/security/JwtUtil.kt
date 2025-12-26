package com.yaojia.restaurant_server.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtUtil(
    @Value("\${jwt.expiration}") private val expirationTime: Long,
    @Value("\${jwt.refresh-expiration}") private val refreshExpirationTime: Long
) {
    private val logger = LoggerFactory.getLogger(JwtUtil::class.java)

    // In production, use a secure key from configuration
    private val secret = "ThisIsASecretKeyForJwtTokenGenerationAndValidation1234567890"
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateToken(username: String, restaurantId: Long, role: String, customerId: Long? = null): String {
        logger.info("Generating access token for user: $username, expires in: $expirationTime ms")
        val builder = Jwts.builder()
            .subject(username)
            .claim("restaurantId", restaurantId)
            .claim("role", role)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expirationTime))
            .signWith(key)
            
        if (customerId != null) {
            builder.claim("customerId", customerId)
        }
            
        return builder.compact()
    }

    fun generateRefreshToken(username: String): String {
        logger.info("Generating refresh token for user: $username, expires in: $refreshExpirationTime ms")
        return Jwts.builder()
            .subject(username)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + refreshExpirationTime))
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
            logger.warn("Token validation error: ${e.message}")
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

    fun getCustomerIdFromToken(token: String): Long? {
        val claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
        
        val id = claims["customerId"] ?: return null
        return if (id is Int) id.toLong() else id as Long
    }
}
