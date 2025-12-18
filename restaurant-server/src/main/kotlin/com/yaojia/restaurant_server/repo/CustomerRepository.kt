package com.yaojia.restaurant_server.repo

import com.yaojia.restaurant_server.data.Customer
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CustomerRepository : CoroutineCrudRepository<Customer, Long> {
    suspend fun findByRestaurantIdAndPhoneNumber(restaurantId: Long, phoneNumber: String): Customer?
}
