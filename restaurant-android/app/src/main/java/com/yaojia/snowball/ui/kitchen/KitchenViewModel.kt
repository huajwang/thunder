package com.yaojia.snowball.ui.kitchen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yaojia.snowball.data.model.Order
import com.yaojia.snowball.data.network.NetworkModule
import kotlinx.coroutines.launch

import android.util.Log

class KitchenViewModel : ViewModel() {

    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> = _orders

    private val _text = MutableLiveData<String>().apply {
        value = "Loading orders..."
    }
    val text: LiveData<String> = _text

    init {
        Log.d("KitchenViewModel", "ViewModel initialized")
        fetchOrders()
        subscribeToUpdates()
    }

    fun fetchOrders() {
        viewModelScope.launch {
            try {
                val restaurantId = NetworkModule.getTokenManager().getRestaurantId()
                Log.d("KitchenViewModel", "Fetching orders for restaurant $restaurantId")
                val result = NetworkModule.apiService.getOrders(restaurantId, listOf("PENDING", "PREPARING"))
                _orders.value = result
                _text.value = "Found ${result.size} pending orders"
            } catch (e: Exception) {
                Log.e("KitchenViewModel", "Error fetching orders", e)
                _text.value = "Error: ${e.message}"
            }
        }
    }

    private fun subscribeToUpdates() {
        viewModelScope.launch {
            try {
                val restaurantId = NetworkModule.getTokenManager().getRestaurantId()
                Log.d("KitchenViewModel", "Subscribing to updates for restaurant $restaurantId")
                NetworkModule.realtimeService.subscribeToOrders(restaurantId).collect {
                    Log.d("KitchenViewModel", "Received update event")
                    // When any event comes, refresh the list
                    fetchOrders()
                }
            } catch (e: Exception) {
                Log.e("KitchenViewModel", "Error subscribing to updates", e)
            }
        }
    }
}
