package com.yaojia.restaurant_server.repo

import com.yaojia.restaurant_server.data.Menu
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MenuRepository : CoroutineCrudRepository<Menu, Long>
