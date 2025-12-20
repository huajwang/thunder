package com.yaojia.restaurant_server

import com.yaojia.restaurant_server.data.Category
import com.yaojia.restaurant_server.data.MenuItem
import com.yaojia.restaurant_server.data.Restaurant
import com.yaojia.restaurant_server.data.RestaurantTable
import com.yaojia.restaurant_server.data.RestaurantVipConfig
import com.yaojia.restaurant_server.data.User
import com.yaojia.restaurant_server.repo.CategoryRepository
import com.yaojia.restaurant_server.repo.MenuItemRepository
import com.yaojia.restaurant_server.repo.RestaurantRepository
import com.yaojia.restaurant_server.repo.RestaurantTableRepository
import com.yaojia.restaurant_server.repo.RestaurantVipConfigRepository
import com.yaojia.restaurant_server.repo.UserRepository
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
    private val restaurantTableRepository: RestaurantTableRepository,
    private val restaurantVipConfigRepository: RestaurantVipConfigRepository,
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(DataSeeder::class.java)

    @Bean
    fun initData() = CommandLineRunner {
        runBlocking {
            seedJoesPizza()
            seedBurgerJoint()
            seedSushiWorld()
            seedTacoFiesta()
            seedUsers()
        }
    }

    private suspend fun seedUsers() {
        seedUser("joes-pizza", "admin", "password")
        seedUser("burger-joint", "admin_burger", "password")
        seedUser("sushi-world", "admin_sushi", "password")
        seedUser("taco-fiesta", "admin_taco", "password")
    }

    private suspend fun seedUser(slug: String, username: String, password: String) {
        if (userRepository.findByUsername(username) == null) {
            val restaurant = restaurantRepository.findBySlug(slug).firstOrNull()
            if (restaurant != null) {
                userRepository.save(
                    User(
                        restaurantId = restaurant.id!!,
                        username = username,
                        password = password,
                        role = "ADMIN"
                    )
                )
                logger.info("Seeded admin user $username for $slug")
            }
        }
    }

    private suspend fun seedJoesPizza() {
        val slug = "joes-pizza"
        val existingRestaurant = restaurantRepository.findBySlug(slug).firstOrNull()
        
        if (existingRestaurant != null) {
            if (restaurantTableRepository.findByRestaurantId(existingRestaurant.id!!).firstOrNull() == null) {
                logger.info("Seeding missing tables for $slug...")
                restaurantTableRepository.save(RestaurantTable(restaurantId = existingRestaurant.id!!, tableNumber = 1))
                restaurantTableRepository.save(RestaurantTable(restaurantId = existingRestaurant.id!!, tableNumber = 2))
                restaurantTableRepository.save(RestaurantTable(restaurantId = existingRestaurant.id!!, tableNumber = 3))
                restaurantTableRepository.save(RestaurantTable(restaurantId = existingRestaurant.id!!, tableNumber = 4))
                restaurantTableRepository.save(RestaurantTable(restaurantId = existingRestaurant.id!!, tableNumber = 5))
            }

            if (restaurantVipConfigRepository.findByRestaurantId(existingRestaurant.id!!) == null) {
                logger.info("Seeding missing VIP config for $slug...")
                restaurantVipConfigRepository.save(
                    RestaurantVipConfig(
                        restaurantId = existingRestaurant.id!!,
                        isEnabled = true,
                        price = BigDecimal("50.00"),
                        description = "Join our VIP club for exclusive benefits!",
                        imageUrl = "https://images.unsplash.com/photo-1568602471122-7832951cc4c5?ixlib=rb-4.0.3&auto=format&fit=crop&w=1470&q=80"
                    )
                )
            }
            
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
                imageUrl = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?ixlib=rb-4.0.3&auto=format&fit=crop&w=1470&q=80",
                address = "123 Main St, New York, NY",
                phoneNumber = "555-0199",
                latitude = 40.7128,
                longitude = -74.0060,
                businessHours = "Mon-Sun: 11:00 AM - 11:00 PM"
            )
        )

        // 2. Create Tables
        restaurantTableRepository.save(RestaurantTable(restaurantId = restaurant.id!!, tableNumber = 1))
        restaurantTableRepository.save(RestaurantTable(restaurantId = restaurant.id!!, tableNumber = 2))
        restaurantTableRepository.save(RestaurantTable(restaurantId = restaurant.id!!, tableNumber = 3))
        restaurantTableRepository.save(RestaurantTable(restaurantId = restaurant.id!!, tableNumber = 4))
        restaurantTableRepository.save(RestaurantTable(restaurantId = restaurant.id!!, tableNumber = 5))

        // 2.5 Create VIP Config
        restaurantVipConfigRepository.save(
            RestaurantVipConfig(
                restaurantId = restaurant.id!!,
                isEnabled = true,
                price = BigDecimal("50.00"),
                description = "Join our VIP club for exclusive benefits!",
                imageUrl = "https://images.unsplash.com/photo-1568602471122-7832951cc4c5?ixlib=rb-4.0.3&auto=format&fit=crop&w=1470&q=80"
            )
        )

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

        logger.info("Seeding Joe's Pizza completed!")
    }

    private suspend fun seedBurgerJoint() {
        val slug = "burger-joint"
        val existingRestaurant = restaurantRepository.findBySlug(slug).firstOrNull()
        
        if (existingRestaurant != null) {
             if (restaurantVipConfigRepository.findByRestaurantId(existingRestaurant.id!!) == null) {
                logger.info("Seeding disabled VIP config for $slug...")
                restaurantVipConfigRepository.save(
                    RestaurantVipConfig(
                        restaurantId = existingRestaurant.id!!,
                        isEnabled = false,
                        price = BigDecimal("0.00")
                    )
                )
            }
            logger.info("Data already seeded for $slug")
            return
        }

        logger.info("Seeding data for $slug...")

        // 1. Create Restaurant
        val restaurant = restaurantRepository.save(
            Restaurant(
                name = "The Burger Joint",
                slug = slug,
                description = "Juicy burgers and crispy fries",
                imageUrl = "https://images.unsplash.com/photo-1586190848861-99c8a3da7ce3?ixlib=rb-4.0.3&auto=format&fit=crop&w=1470&q=80",
                address = "456 Burger Lane, Mountain View, CA",
                phoneNumber = "555-0200",
                latitude = 37.3861,
                longitude = -122.0839,
                businessHours = "Mon-Sat: 11:00 AM - 10:00 PM, Sun: 12:00 PM - 9:00 PM"
            )
        )

        // 2. Create Tables
        restaurantTableRepository.save(RestaurantTable(restaurantId = restaurant.id!!, tableNumber = 1))
        restaurantTableRepository.save(RestaurantTable(restaurantId = restaurant.id!!, tableNumber = 2))

        // 2.5 Create VIP Config (Disabled)
        restaurantVipConfigRepository.save(
            RestaurantVipConfig(
                restaurantId = restaurant.id!!,
                isEnabled = false,
                price = BigDecimal("0.00")
            )
        )

        // 3. Create Categories
        val burgers = categoryRepository.save(
            Category(restaurantId = restaurant.id!!, name = "Burgers", displayOrder = 1)
        )
        val sides = categoryRepository.save(
            Category(restaurantId = restaurant.id!!, name = "Sides", displayOrder = 2)
        )
        val drinks = categoryRepository.save(
            Category(restaurantId = restaurant.id!!, name = "Drinks", displayOrder = 3)
        )

        // 4. Create Menu Items
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = burgers.id,
                name = "Classic Burger",
                description = "Beef patty, lettuce, tomato, onion, pickles",
                price = BigDecimal("10.99"),
                imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = burgers.id,
                name = "Cheeseburger",
                description = "Classic burger with cheddar cheese",
                price = BigDecimal("11.99"),
                imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=500&q=60"
            )
        )
         menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = sides.id,
                name = "Fries",
                description = "Crispy fries",
                price = BigDecimal("3.99"),
                imageUrl = "https://images.unsplash.com/photo-1573080496987-a199f8cd75c5?auto=format&fit=crop&w=500&q=60"
            )
        )
         menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = drinks.id,
                name = "Soda",
                description = "Fountain drink",
                price = BigDecimal("2.50"),
                imageUrl = "https://images.unsplash.com/photo-1622483767028-3f66f32aef97?auto=format&fit=crop&w=500&q=60"
            )
        )
        
        logger.info("Seeding Burger Joint completed!")
    }

    private suspend fun seedSushiWorld() {
        val slug = "sushi-world"
        val existingRestaurant = restaurantRepository.findBySlug(slug).firstOrNull()
        
        if (existingRestaurant != null) {
            logger.info("Data already seeded for $slug")
            return
        }

        logger.info("Seeding data for $slug...")

        // 1. Create Restaurant
        val restaurant = restaurantRepository.save(
            Restaurant(
                name = "Sushi World",
                slug = slug,
                description = "Fresh and authentic Japanese sushi",
                imageUrl = "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?ixlib=rb-4.0.3&auto=format&fit=crop&w=1470&q=80",
                address = "583 King St N, Waterloo, ON N2V 2E5",
                phoneNumber = "519-789-0300",
                latitude = 43.50179,
                longitude = -80.53403,
                businessHours = "Tue-Sun: 12:00 PM - 10:00 PM, Mon: Closed"
            )
        )

        // 2. Create Tables
        restaurantTableRepository.save(RestaurantTable(restaurantId = restaurant.id!!, tableNumber = 1))
        restaurantTableRepository.save(RestaurantTable(restaurantId = restaurant.id!!, tableNumber = 2))
        restaurantTableRepository.save(RestaurantTable(restaurantId = restaurant.id!!, tableNumber = 3))

        // 2.5 Create VIP Config
        restaurantVipConfigRepository.save(
            RestaurantVipConfig(
                restaurantId = restaurant.id!!,
                isEnabled = true,
                price = BigDecimal("100.00"),
                description = "Exclusive Omakase Access",
                imageUrl = "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?ixlib=rb-4.0.3&auto=format&fit=crop&w=1470&q=80"
            )
        )

        // 3. Create Categories
        val sushi = categoryRepository.save(
            Category(restaurantId = restaurant.id!!, name = "Sushi", displayOrder = 1)
        )
        val rolls = categoryRepository.save(
            Category(restaurantId = restaurant.id!!, name = "Rolls", displayOrder = 2)
        )
        val drinks = categoryRepository.save(
            Category(restaurantId = restaurant.id!!, name = "Drinks", displayOrder = 3)
        )

        // 4. Create Menu Items
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = sushi.id,
                name = "Salmon Nigiri",
                description = "Fresh salmon over rice",
                price = BigDecimal("6.00"),
                imageUrl = "https://images.unsplash.com/photo-1611143669185-af224c5e3252?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = sushi.id,
                name = "Tuna Nigiri",
                description = "Fresh tuna over rice",
                price = BigDecimal("7.00"),
                imageUrl = "https://images.unsplash.com/photo-1598514982205-f36b96d1e8d4?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = rolls.id,
                name = "California Roll",
                description = "Crab, avocado, cucumber",
                price = BigDecimal("8.00"),
                imageUrl = "https://images.unsplash.com/photo-1579584425555-c3ce17fd436d?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = rolls.id,
                name = "Spicy Tuna Roll",
                description = "Spicy tuna, cucumber",
                price = BigDecimal("9.00"),
                imageUrl = "https://images.unsplash.com/photo-1579584425555-c3ce17fd436d?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = drinks.id,
                name = "Green Tea",
                description = "Hot green tea",
                price = BigDecimal("2.00"),
                imageUrl = "https://images.unsplash.com/photo-1627435601361-ec25f5b1d0e5?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = drinks.id,
                name = "Sake",
                description = "Japanese rice wine",
                price = BigDecimal("8.00"),
                imageUrl = "https://images.unsplash.com/photo-1582234279500-4b9f9e2b5b9e?auto=format&fit=crop&w=500&q=60"
            )
        )

        logger.info("Seeding Sushi World completed!")
    }

    private suspend fun seedTacoFiesta() {
        val slug = "taco-fiesta"
        val existingRestaurant = restaurantRepository.findBySlug(slug).firstOrNull()
        
        if (existingRestaurant != null) {
            logger.info("Data already seeded for $slug")
            return
        }

        logger.info("Seeding data for $slug...")

        // 1. Create Restaurant
        val restaurant = restaurantRepository.save(
            Restaurant(
                name = "Taco Fiesta",
                slug = slug,
                description = "Authentic Mexican street food",
                imageUrl = "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?ixlib=rb-4.0.3&auto=format&fit=crop&w=1470&q=80",
                address = "321 Taco Blvd, Salsa City, SC",
                phoneNumber = "555-0400",
                latitude = 19.4326,
                longitude = -99.1332,
                businessHours = "Mon-Sun: 10:00 AM - 12:00 AM"
            )
        )

        // 2. Create Tables
        restaurantTableRepository.save(RestaurantTable(restaurantId = restaurant.id!!, tableNumber = 1))
        restaurantTableRepository.save(RestaurantTable(restaurantId = restaurant.id!!, tableNumber = 2))
        restaurantTableRepository.save(RestaurantTable(restaurantId = restaurant.id!!, tableNumber = 3))
        restaurantTableRepository.save(RestaurantTable(restaurantId = restaurant.id!!, tableNumber = 4))

        // 2.5 Create VIP Config (Disabled)
        restaurantVipConfigRepository.save(
            RestaurantVipConfig(
                restaurantId = restaurant.id!!,
                isEnabled = false,
                price = BigDecimal("0.00")
            )
        )

        // 3. Create Categories
        val tacos = categoryRepository.save(
            Category(restaurantId = restaurant.id!!, name = "Tacos", displayOrder = 1)
        )
        val burritos = categoryRepository.save(
            Category(restaurantId = restaurant.id!!, name = "Burritos", displayOrder = 2)
        )
        val sides = categoryRepository.save(
            Category(restaurantId = restaurant.id!!, name = "Sides", displayOrder = 3)
        )

        // 4. Create Menu Items
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = tacos.id,
                name = "Carne Asada Taco",
                description = "Grilled steak taco with onions and cilantro",
                price = BigDecimal("3.50"),
                imageUrl = "https://images.unsplash.com/photo-1551504734-5ee1c4a1479b?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = tacos.id,
                name = "Al Pastor Taco",
                description = "Marinated pork taco with pineapple",
                price = BigDecimal("3.50"),
                imageUrl = "https://images.unsplash.com/photo-1551504734-5ee1c4a1479b?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = burritos.id,
                name = "Bean and Cheese Burrito",
                description = "Beans and cheese wrapped in a flour tortilla",
                price = BigDecimal("6.00"),
                imageUrl = "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = burritos.id,
                name = "Chicken Burrito",
                description = "Grilled chicken, rice, beans, and salsa",
                price = BigDecimal("8.00"),
                imageUrl = "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?auto=format&fit=crop&w=500&q=60"
            )
        )
        menuItemRepository.save(
            MenuItem(
                restaurantId = restaurant.id!!,
                categoryId = sides.id,
                name = "Chips and Guacamole",
                description = "Fresh tortilla chips with homemade guacamole",
                price = BigDecimal("5.00"),
                imageUrl = "https://images.unsplash.com/photo-1576097449798-7c7f90e123a6?auto=format&fit=crop&w=500&q=60"
            )
        )

        logger.info("Seeding Taco Fiesta completed!")
    }
}
