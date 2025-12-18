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
        val pizzas = categoryRepository.save(
            Category(restaurantId = restaurant.id!!, name = "Pizzas", displayOrder = 2)
        )
        val pastas = categoryRepository.save(
            Category(restaurantId = restaurant.id!!, name = "Pastas", displayOrder = 3)
        )
        val desserts = categoryRepository.save(
            Category(restaurantId = restaurant.id!!, name = "Desserts", displayOrder = 4)
        )
        val drinks = categoryRepository.save(
            Category(restaurantId = restaurant.id!!, name = "Drinks", displayOrder = 5)
        )
        val salads = categoryRepository.save(
            Category(restaurantId = restaurant.id!!, name = "Salads", displayOrder = 6)
        )
        val soups = categoryRepository.save(
            Category(restaurantId = restaurant.id!!, name = "Soups", displayOrder = 7)
        )
        val sandwiches = categoryRepository.save(
            Category(restaurantId = restaurant.id!!, name = "Sandwiches", displayOrder = 8)
        )
        val sides = categoryRepository.save(
            Category(restaurantId = restaurant.id!!, name = "Sides", displayOrder = 9)
        )
        val kids = categoryRepository.save(
            Category(restaurantId = restaurant.id!!, name = "Kids Menu", displayOrder = 10)
        )

        // 4. Create Menu Items
        // Starters
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = starters.id,
                name = "Garlic Knots",
                description = "Oven-baked dough knots with garlic oil and parmesan",
                price = BigDecimal("5.99"),
                imageUrl = "https://images.unsplash.com/photo-1592417817098-8fd3d9eb14a5?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = starters.id,
                name = "Caprese Salad",
                description = "Fresh mozzarella, tomatoes, basil, and balsamic glaze",
                price = BigDecimal("9.50"),
                imageUrl = "https://images.unsplash.com/photo-1592417817098-8fd3d9eb14a5?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = starters.id,
                name = "Bruschetta",
                description = "Toasted bread topped with diced tomatoes, garlic, and basil",
                price = BigDecimal("7.99"),
                imageUrl = "https://images.unsplash.com/photo-1506280754576-f6fa8a873550?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = starters.id,
                name = "Mozzarella Sticks",
                description = "Fried mozzarella cheese sticks served with marinara sauce",
                price = BigDecimal("8.99"),
                imageUrl = "https://images.unsplash.com/photo-1531749668029-2db88e4276c7?auto=format&fit=crop&w=500&q=60"
            )
        )

        // Pizzas
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = pizzas.id,
                name = "Margherita Pizza",
                description = "Classic tomato sauce, fresh mozzarella, and basil",
                price = BigDecimal("14.00"),
                imageUrl = "https://images.unsplash.com/photo-1574071318508-1cdbab80d002?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = pizzas.id,
                name = "Pepperoni Pizza",
                description = "Tomato sauce, mozzarella, and spicy pepperoni slices",
                price = BigDecimal("16.00"),
                imageUrl = "https://images.unsplash.com/photo-1628840042765-356cda07504e?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = pizzas.id,
                name = "Hawaiian Pizza",
                description = "Tomato sauce, mozzarella, ham, and pineapple",
                price = BigDecimal("16.50"),
                imageUrl = "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = pizzas.id,
                name = "Veggie Supreme",
                description = "Tomato sauce, mozzarella, bell peppers, onions, mushrooms, and olives",
                price = BigDecimal("17.00"),
                imageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = pizzas.id,
                name = "Meat Lovers",
                description = "Tomato sauce, mozzarella, pepperoni, sausage, bacon, and ham",
                price = BigDecimal("19.00"),
                imageUrl = "https://images.unsplash.com/photo-1604382354936-07c5d9983bd3?auto=format&fit=crop&w=500&q=60"
            )
        )

        // Pastas
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = pastas.id,
                name = "Spaghetti Carbonara",
                description = "Spaghetti with creamy egg sauce, pancetta, and parmesan",
                price = BigDecimal("15.50"),
                imageUrl = "https://images.unsplash.com/photo-1612874742237-6526221588e3?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = pastas.id,
                name = "Fettuccine Alfredo",
                description = "Fettuccine pasta tossed in a rich creamy parmesan sauce",
                price = BigDecimal("14.50"),
                imageUrl = "https://images.unsplash.com/photo-1645112411341-6c4fd023714a?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = pastas.id,
                name = "Penne Arrabbiata",
                description = "Penne pasta in a spicy tomato sauce with garlic and chili",
                price = BigDecimal("13.50"),
                imageUrl = "https://images.unsplash.com/photo-1608835291093-394b0c943a75?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = pastas.id,
                name = "Classic Lasagna",
                description = "Layers of pasta, meat sauce, ricotta, and mozzarella cheese",
                price = BigDecimal("16.99"),
                imageUrl = "https://images.unsplash.com/photo-1608835291093-394b0c943a75?auto=format&fit=crop&w=500&q=60"
            )
        )

        // Desserts
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = desserts.id,
                name = "Tiramisu",
                description = "Classic Italian dessert with coffee-soaked ladyfingers and mascarpone cream",
                price = BigDecimal("7.50"),
                imageUrl = "https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = desserts.id,
                name = "Cannoli",
                description = "Crispy pastry shells filled with sweet ricotta cream and chocolate chips",
                price = BigDecimal("6.00"),
                imageUrl = "https://images.unsplash.com/photo-1551024709-8f23befc6f87?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = desserts.id,
                name = "NY Cheesecake",
                description = "Rich and creamy cheesecake with a graham cracker crust",
                price = BigDecimal("8.00"),
                imageUrl = "https://images.unsplash.com/photo-1508737027454-e6454ef45afd?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = desserts.id,
                name = "Chocolate Lava Cake",
                description = "Warm chocolate cake with a molten chocolate center, served with vanilla ice cream",
                price = BigDecimal("9.00"),
                imageUrl = "https://images.unsplash.com/photo-1624353365286-3f8d62daad51?auto=format&fit=crop&w=500&q=60"
            )
        )

        // Drinks
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = drinks.id,
                name = "Coca-Cola",
                description = "Classic Coke",
                price = BigDecimal("2.50"),
                imageUrl = "https://images.unsplash.com/photo-1622483767028-3f66f32aef97?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = drinks.id,
                name = "Diet Coke",
                description = "Diet Coke",
                price = BigDecimal("2.50"),
                imageUrl = "https://images.unsplash.com/photo-1622483767028-3f66f32aef97?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = drinks.id,
                name = "Sprite",
                description = "Lemon-lime soda",
                price = BigDecimal("2.50"),
                imageUrl = "https://images.unsplash.com/photo-1625772299848-391b6a87d7b3?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = drinks.id,
                name = "Iced Tea",
                description = "Freshly brewed iced tea, sweetened or unsweetened",
                price = BigDecimal("3.00"),
                imageUrl = "https://images.unsplash.com/photo-1556679343-c7306c1976bc?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = drinks.id,
                name = "Lemonade",
                description = "Freshly squeezed lemonade",
                price = BigDecimal("3.50"),
                imageUrl = "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = drinks.id,
                name = "Espresso",
                description = "Single shot of rich espresso",
                price = BigDecimal("3.00"),
                imageUrl = "https://images.unsplash.com/photo-1510707577719-ae7c14805e3a?auto=format&fit=crop&w=500&q=60"
            )
        )

        // Salads
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = salads.id,
                name = "Caesar Salad",
                description = "Romaine lettuce, croutons, parmesan cheese, and Caesar dressing",
                price = BigDecimal("10.50"),
                imageUrl = "https://images.unsplash.com/photo-1550304943-4f24f54ddde9?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = salads.id,
                name = "Greek Salad",
                description = "Cucumbers, tomatoes, olives, feta cheese, and red onion",
                price = BigDecimal("11.00"),
                imageUrl = "https://images.unsplash.com/photo-1540189549336-e6e99c3679fe?auto=format&fit=crop&w=500&q=60"
            )
        )

        // Soups
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = soups.id,
                name = "Tomato Basil Soup",
                description = "Creamy tomato soup with fresh basil",
                price = BigDecimal("6.50"),
                imageUrl = "https://images.unsplash.com/photo-1547592180-85f173990554?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = soups.id,
                name = "Minestrone",
                description = "Classic Italian vegetable soup with pasta",
                price = BigDecimal("7.00"),
                imageUrl = "https://images.unsplash.com/photo-1547592180-85f173990554?auto=format&fit=crop&w=500&q=60"
            )
        )

        // Sandwiches
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = sandwiches.id,
                name = "Chicken Parm Sandwich",
                description = "Breaded chicken cutlet with marinara and melted mozzarella",
                price = BigDecimal("12.50"),
                imageUrl = "https://images.unsplash.com/photo-1521390188846-e2a3a97453a0?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = sandwiches.id,
                name = "Italian Sub",
                description = "Salami, pepperoni, ham, provolone, lettuce, tomato, and oil & vinegar",
                price = BigDecimal("13.00"),
                imageUrl = "https://images.unsplash.com/photo-1521390188846-e2a3a97453a0?auto=format&fit=crop&w=500&q=60"
            )
        )

        // Sides
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = sides.id,
                name = "French Fries",
                description = "Crispy golden fries",
                price = BigDecimal("4.50"),
                imageUrl = "https://images.unsplash.com/photo-1630384060421-cb20d0e0649d?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = sides.id,
                name = "Onion Rings",
                description = "Beer-battered onion rings",
                price = BigDecimal("5.50"),
                imageUrl = "https://images.unsplash.com/photo-1639024471283-03518883512d?auto=format&fit=crop&w=500&q=60"
            )
        )

        // Kids Menu
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = kids.id,
                name = "Kids Pizza",
                description = "Small cheese pizza",
                price = BigDecimal("8.00"),
                imageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = kids.id,
                name = "Kids Spaghetti",
                description = "Spaghetti with butter or marinara sauce",
                price = BigDecimal("7.50"),
                imageUrl = "https://images.unsplash.com/photo-1551892374-ecf8754cf8b0?auto=format&fit=crop&w=500&q=60"
            )
        )

        logger.info("Seeding completed!")
    }
}
