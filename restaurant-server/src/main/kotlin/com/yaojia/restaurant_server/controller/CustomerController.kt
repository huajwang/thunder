package com.yaojia.restaurant_server.controller

import com.yaojia.restaurant_server.data.Customer
import com.yaojia.restaurant_server.data.RewardPointTransaction
import com.yaojia.restaurant_server.repo.CustomerRepository
import com.yaojia.restaurant_server.repo.RestaurantRepository
import com.yaojia.restaurant_server.repo.RewardPointTransactionRepository
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.ConcurrentHashMap

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList

data class EnrollRequest(val phoneNumber: String)
data class LoginRequest(val phoneNumber: String, val code: String)
data class LoginResponse(val customerId: Long, val phoneNumber: String, val isMember: Boolean, val totalRewardPoints: Int)

@RestController
@RequestMapping("/api/customers")
class CustomerController(
    private val customerRepository: CustomerRepository,
    private val restaurantRepository: RestaurantRepository,
    private val rewardPointTransactionRepository: RewardPointTransactionRepository
) {
    // In-memory store for OTPs (Simulated)
    private val otpStore = ConcurrentHashMap<String, String>()

    @PostMapping("/enroll")
    suspend fun enrollCustomer(
        @RequestParam restaurantId: Long,
        @RequestBody request: EnrollRequest,
        authentication: Authentication
    ): Customer {
        // Verify staff access
        val details = authentication.details as? Map<*, *>
        val userRestaurantId = details?.get("restaurantId") as? Long
        if (userRestaurantId != null && userRestaurantId != restaurantId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied")
        }

        val existing = customerRepository.findByRestaurantIdAndPhoneNumber(restaurantId, request.phoneNumber)
        if (existing != null) {
            if (existing.isMember) {
                throw ResponseStatusException(HttpStatus.CONFLICT, "Customer is already a member")
            }
            // Upgrade to member
            return customerRepository.save(existing.copy(isMember = true))
        }

        return customerRepository.save(
            Customer(
                restaurantId = restaurantId,
                phoneNumber = request.phoneNumber,
                isMember = true
            )
        )
    }

    @PostMapping("/login/request-code")
    suspend fun requestLoginCode(@RequestParam restaurantId: Long, @RequestBody request: EnrollRequest) {
        // Generate OTP (Fixed for dev)
        val otp = "123456"
        otpStore["$restaurantId:${request.phoneNumber}"] = otp
        println("OTP for ${request.phoneNumber} at restaurant $restaurantId: $otp")
    }

    @PostMapping("/login")
    suspend fun login(@RequestParam restaurantId: Long, @RequestBody request: LoginRequest): LoginResponse {
        val storedOtp = otpStore["$restaurantId:${request.phoneNumber}"]
        if (storedOtp == null || storedOtp != request.code) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid code")
        }

        var customer = customerRepository.findByRestaurantIdAndPhoneNumber(restaurantId, request.phoneNumber)
        
        if (customer == null) {
            // Create new customer on first login
            customer = customerRepository.save(
                Customer(
                    restaurantId = restaurantId,
                    phoneNumber = request.phoneNumber,
                    isMember = false
                )
            )
        }

        otpStore.remove("$restaurantId:${request.phoneNumber}")

        return LoginResponse(
            customerId = customer!!.id!!,
            phoneNumber = customer.phoneNumber,
            isMember = customer.isMember,
            totalRewardPoints = customer.totalRewardPoints
        )
    }

    @GetMapping("/{customerId}/rewards")
    suspend fun getRewardHistory(
        @PathVariable customerId: Long,
        @RequestParam restaurantId: Long
    ): Flow<RewardPointTransaction> {
        println("Fetching rewards for customer: $customerId")
        val rewards = rewardPointTransactionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
        return rewards
    }
}
