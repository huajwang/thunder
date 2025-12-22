package com.yaojia.snowball

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.yaojia.snowball.data.network.LogoutReason
import com.yaojia.snowball.data.network.NetworkModule
import com.yaojia.snowball.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (NetworkModule.getTokenManager().getToken() == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Observe logout events
        lifecycleScope.launch {
            NetworkModule.getTokenManager().logoutEvent.collect { reason ->
                Log.d("MainActivity", "Logout event received: $reason")
                if (reason == LogoutReason.SESSION_EXPIRED) {
                    showSessionExpiredDialog()
                } else {
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    finish()
                }
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        val restaurantId = NetworkModule.getTokenManager().getRestaurantId()
        (binding.appBarMain.fab as? com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton)?.text = "Restaurant $restaurantId"

        binding.appBarMain.fab?.setOnClickListener { view ->
            Snackbar.make(view, "Restaurant ID: $restaurantId", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
        }

        lifecycleScope.launch {
            try {
                val restaurant = NetworkModule.apiService.getRestaurant(restaurantId)
                
                // Update FAB
                (binding.appBarMain.fab as? com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton)?.text = restaurant.name
                binding.appBarMain.fab?.setOnClickListener { view ->
                    Snackbar.make(view, "Restaurant: ${restaurant.name}", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .setAnchorView(R.id.fab).show()
                }

                // Update Nav Header
                binding.navView?.let { navView ->
                    val headerView = navView.getHeaderView(0)
                    val tvTitle = headerView.findViewById<android.widget.TextView>(R.id.tv_header_title)
                    val tvSubtitle = headerView.findViewById<android.widget.TextView>(R.id.tv_header_subtitle)
                    val imageView = headerView.findViewById<android.widget.ImageView>(R.id.imageView)
                    
                    tvTitle.text = restaurant.name
                    tvSubtitle.text = restaurant.address ?: "ID: $restaurantId"
                    
                    if (!restaurant.imageUrl.isNullOrEmpty()) {
                        Glide.with(this@MainActivity)
                            .load(restaurant.imageUrl)
                            .circleCrop()
                            .placeholder(R.mipmap.ic_launcher_round)
                            .error(R.mipmap.ic_launcher_round)
                            .into(imageView)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val navHostFragment =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment?)!!
        val navController = navHostFragment.navController

        binding.navView?.let {
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_kitchen, R.id.nav_staff, R.id.nav_tables
                ),
                binding.drawerLayout
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            it.setupWithNavController(navController)

            val headerView = it.getHeaderView(0)
            val tvTitle = headerView.findViewById<android.widget.TextView>(R.id.tv_header_title)
            val tvSubtitle = headerView.findViewById<android.widget.TextView>(R.id.tv_header_subtitle)
            tvTitle.text = "Restaurant $restaurantId"
            tvSubtitle.text = "ID: $restaurantId"
        }

        binding.appBarMain.contentMain.bottomNavView?.let {
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_kitchen, R.id.nav_staff, R.id.nav_tables
                )
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            it.setupWithNavController(navController)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val result = super.onCreateOptionsMenu(menu)
        // Using findViewById because NavigationView exists in different layout files
        // between w600dp and w1240dp
        val navView: NavigationView? = findViewById(R.id.nav_view)
        if (navView == null) {
            // The navigation drawer already has the items including the items in the overflow menu
            // We only inflate the overflow menu if the navigation drawer isn't visible
            menuInflater.inflate(R.menu.overflow, menu)
        }
        return result
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun showSessionExpiredDialog() {
        AlertDialog.Builder(this)
            .setTitle("Session Expired")
            .setMessage("Your session has expired. Please log in again.")
            .setCancelable(false)
            .setPositiveButton("Log In") { _, _ ->
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .show()
    }
}