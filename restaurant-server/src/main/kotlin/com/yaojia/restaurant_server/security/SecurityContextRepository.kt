package com.yaojia.restaurant_server.security

import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

import org.slf4j.LoggerFactory

@Component
class SecurityContextRepository(
    private val authenticationManager: AuthenticationManager
) : ServerSecurityContextRepository {
    private val logger = LoggerFactory.getLogger(SecurityContextRepository::class.java)

    override fun save(exchange: ServerWebExchange, context: SecurityContext?): Mono<Void> {
        throw UnsupportedOperationException("Not supported yet.")
    }

    override fun load(exchange: ServerWebExchange): Mono<SecurityContext> {
        var authToken: String? = null
        val authHeader = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authToken = authHeader.substring(7)
        } else {
            // Check for token in query parameter (for SSE)
            authToken = exchange.request.queryParams.getFirst("token")
        }

        if (authToken != null) {
            logger.info("Found token: ${authToken.take(10)}...")
            val auth = UsernamePasswordAuthenticationToken(authToken, authToken)
            return authenticationManager.authenticate(auth)
                .map { SecurityContextImpl(it) as SecurityContext }
                .doOnError { e -> logger.error("Authentication failed", e) }
        } else {
            logger.info("No token found in request to ${exchange.request.path}")
        }
        
        return Mono.empty()
    }
}
