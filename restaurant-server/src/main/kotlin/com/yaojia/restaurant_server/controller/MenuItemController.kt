package com.yaojia.restaurant_server.controller

import com.yaojia.restaurant_server.data.MenuItem
import com.yaojia.restaurant_server.repo.MenuItemRepository
import com.yaojia.restaurant_server.repo.MenuItemVariantRepository
import kotlinx.coroutines.flow.toList
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/menu-items")
class MenuItemController(
    private val menuItemRepository: MenuItemRepository,
    private val menuItemVariantRepository: MenuItemVariantRepository
) {

    @GetMapping("/{id}")
    suspend fun getMenuItem(@PathVariable id: Long): MenuItem {
        val item = menuItemRepository.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Menu item not found")
        
        val variants = menuItemVariantRepository.findByMenuItemId(id).toList()
        item.variants = variants
        return item
    }
}
