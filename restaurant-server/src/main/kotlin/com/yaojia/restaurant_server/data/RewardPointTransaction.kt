package com.yaojia.restaurant_server.data

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("reward_point_transactions")
data class RewardPointTransaction(
    @Id
    val id: Long? = null,
    val customerId: Long,
    val orderId: Long? = null,
    val points: Int,
    val type: TransactionType,
    val description: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class TransactionType {
    EARNED, REDEEMED, ADJUSTMENT
}
