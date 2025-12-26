package com.yaojia.restaurant_server.service

import com.yaojia.restaurant_server.data.Restaurant
import com.yaojia.restaurant_server.dto.CategoryWithItems
import com.yaojia.restaurant_server.dto.MenuResponse
import com.yaojia.restaurant_server.repo.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.time.ZonedDateTime

@Service
class MenuService(
    private val restaurantRepository: RestaurantRepository,
    private val menuRepository: MenuRepository,
    private val menuScheduleRepository: MenuScheduleRepository,
    private val menuHolidayOverrideRepository: MenuHolidayOverrideRepository,
    private val menuItemRepository: MenuItemRepository,
    private val menuItemVariantRepository: MenuItemVariantRepository,
    private val categoryRepository: CategoryRepository
) {

    suspend fun getActiveMenu(restaurantSlug: String): MenuResponse? {
        val restaurant = restaurantRepository.findBySlug(restaurantSlug).firstOrNull()
            ?: throw IllegalArgumentException("Restaurant not found")

        return getActiveMenuForRestaurant(restaurant)
    }

    suspend fun getActiveMenuForRestaurant(restaurant: Restaurant): MenuResponse? {
        val timezone = try {
            ZoneId.of(restaurant.timezone)
        } catch (e: Exception) {
            ZoneId.of("America/Toronto") // Fallback
        }
        
        val now = ZonedDateTime.now(timezone)
        val currentDate = now.toLocalDate()
        val currentTime = now.toLocalTime()
        val currentDayOfWeek = now.dayOfWeek.name // MONDAY, TUESDAY...

        // 1. Check Holiday Overrides
        val overrides = menuHolidayOverrideRepository.findByRestaurantIdAndOverrideDate(restaurant.id!!, currentDate).toList()
        val activeOverride = overrides.find {
            !currentTime.isBefore(it.startTime) && !currentTime.isAfter(it.endTime)
        }

        var activeMenuId: Long? = activeOverride?.menuId

        // 2. If no override, check Schedules
        if (activeMenuId == null) {
            val schedules = menuScheduleRepository.findByRestaurantId(restaurant.id).toList()
            val activeSchedule = schedules.find {
                it.dayOfWeek == currentDayOfWeek &&
                !currentTime.isBefore(it.startTime) &&
                !currentTime.isAfter(it.endTime)
            }
            activeMenuId = activeSchedule?.menuId
        }

        if (activeMenuId == null) {
            return null
        }

        val menu = menuRepository.findById(activeMenuId) ?: return null
        if (!menu.isActive) return null

        val items = menuItemRepository.findByMenuId(activeMenuId).toList()

        // Populate variants
        val itemIds = items.mapNotNull { it.id }
        if (itemIds.isNotEmpty()) {
            val variants = menuItemVariantRepository.findByMenuItemIdIn(itemIds).toList()
            val variantsMap = variants.groupBy { it.menuItemId }
            items.forEach { item ->
                item.variants = variantsMap[item.id] ?: emptyList()
            }
        }

        // Group by Category
        val categoryIds = items.mapNotNull { it.categoryId }.distinct()
        val categories = categoryRepository.findAllById(categoryIds).toList().sortedBy { it.displayOrder }
        
        val itemsByCategory = items.groupBy { it.categoryId }

        val categoriesWithItems = categories.map { category ->
            CategoryWithItems(
                id = category.id!!,
                name = category.name,
                displayOrder = category.displayOrder,
                items = itemsByCategory[category.id] ?: emptyList()
            )
        }

        return MenuResponse(
            id = menu.id!!,
            name = menu.name,
            description = menu.description,
            categories = categoriesWithItems
        )
    }
}
