package com.yaojia.restaurant_server

import com.yaojia.restaurant_server.data.Category
import com.yaojia.restaurant_server.data.MenuItem
import com.yaojia.restaurant_server.data.Restaurant
import com.yaojia.restaurant_server.data.RestaurantTable
import com.yaojia.restaurant_server.repo.CategoryRepository
import com.yaojia.restaurant_server.repo.MenuItemRepository
import com.yaojia.restaurant_server.repo.RestaurantRepository
import com.yaojia.restaurant_server.repo.RestaurantTableRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.math.BigDecimal

@Configuration
@Profile("dev") // Only run in dev profile
class DataSeeder(
    private val restaurantRepository: RestaurantRepository,
    private val categoryRepository: CategoryRepository,
    private val menuItemRepository: MenuItemRepository,
    private val restaurantTableRepository: RestaurantTableRepository
) {
    private val logger = LoggerFactory.getLogger(DataSeeder::class.java)

    @Bean
    fun initData() = CommandLineRunner {
        runBlocking {
            seedRestaurants()
        }
    }

    private suspend fun seedRestaurants() {
        val slug = "joes-pizza"
        if (restaurantRepository.findBySlug(slug).firstOrNull() != null) {
            logger.info("Data already seeded for $slug")
            return
        }

        logger.info("Seeding data for $slug...")

        // 1. Create Restaurant
        val restaurant = restaurantRepository.save(
            Restaurant(
                name = "Joe's Pizza",
                slug = slug,
                description = "Best Pizza in Town since 1995",
                address = "123 Main St, New York, NY",
                phoneNumber = "555-0199"
            )
        )

        // 2. Create Tables
        restaurantTableRepository.save(RestaurantTable(restaurantId = restaurant.id!!, tableNumber = 1))
        restaurantTableRepository.save(RestaurantTable(restaurantId = restaurant.id!!, tableNumber = 2))
        restaurantTableRepository.save(RestaurantTable(restaurantId = restaurant.id!!, tableNumber = 3))
        restaurantTableRepository.save(RestaurantTable(restaurantId = restaurant.id!!, tableNumber = 4))
        restaurantTableRepository.save(RestaurantTable(restaurantId = restaurant.id!!, tableNumber = 5))

        // 3. Create Categories
        val starters = categoryRepository.save(
            Category(restaurantId = restaurant.id!!, name = "Starters", displayOrder = 1)
        )
        val mains = categoryRepository.save(
            Category(restaurantId = restaurant.id!!, name = "Mains", displayOrder = 2)
        )
        val drinks = categoryRepository.save(
            Category(restaurantId = restaurant.id!!, name = "Drinks", displayOrder = 3)
        )

        // 3. Create Menu Items
        // Starters
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = starters.id,
                name = "Garlic Knots",
                description = "Oven-baked dough knots with garlic oil",
                price = BigDecimal("5.99")
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = starters.id,
                name = "Caprese Salad",
                description = "Fresh mozzarella, tomatoes, and basil",
                price = BigDecimal("8.50")
            )
        )

        // Mains
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = mains.id,
                name = "Pepperoni Pizza",
                description = "Classic pepperoni with mozzarella",
                price = BigDecimal("14.00")
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = mains.id,
                name = "Veggie Supreme",
                description = "Peppers, onions, mushrooms, and olives",
                price = BigDecimal("16.50")
            )
        )

        // Drinks
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = drinks.id,
                name = "Coke",
                price = BigDecimal("2.50")
            )
        )

        logger.info("Seeding completed!")
    }
}
