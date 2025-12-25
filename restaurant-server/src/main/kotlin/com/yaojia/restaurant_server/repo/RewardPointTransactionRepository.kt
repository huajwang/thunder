package com.yaojia.restaurant_server.repo

import com.yaojia.restaurant_server.data.RewardPointTransaction
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RewardPointTransactionRepository : CoroutineCrudRepository<RewardPointTransaction, Long> {
    fun findByCustomerIdOrderByCreatedAtDesc(customerId: Long): Flow<RewardPointTransaction>
}
