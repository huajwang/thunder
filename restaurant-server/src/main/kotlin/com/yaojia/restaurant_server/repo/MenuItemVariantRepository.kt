package com.yaojia.restaurant_server.repo

import com.yaojia.restaurant_server.data.MenuItemVariant
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import kotlinx.coroutines.flow.Flow

@Repository
interface MenuItemVariantRepository : CoroutineCrudRepository<MenuItemVariant, Long> {
    fun findByMenuItemId(menuItemId: Long): Flow<MenuItemVariant>
    fun findByMenuItemIdIn(menuItemIds: List<Long>): Flow<MenuItemVariant>
}
