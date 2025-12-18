package com.yaojia.restaurant_server.repo

import com.yaojia.restaurant_server.data.User
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : CoroutineCrudRepository<User, Long> {
    suspend fun findByUsername(username: String): User?
}
