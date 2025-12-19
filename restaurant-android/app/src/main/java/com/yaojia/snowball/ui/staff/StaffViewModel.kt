package com.yaojia.snowball.ui.staff

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yaojia.snowball.data.model.Order
import com.yaojia.snowball.data.network.NetworkModule
import kotlinx.coroutines.launch

class StaffViewModel : ViewModel() {

    private val _readyOrders = MutableLiveData<List<Order>>()
    val readyOrders: LiveData<List<Order>> = _readyOrders

    private val _text = MutableLiveData<String>().apply {
        value = "Loading ready orders..."
    }
    val text: LiveData<String> = _text

    init {
        fetchReadyOrders()
        subscribeToUpdates()
    }

    fun fetchReadyOrders() {
        viewModelScope.launch {
            try {
                val restaurantId = NetworkModule.getTokenManager().getRestaurantId()
                val result = NetworkModule.apiService.getOrders(restaurantId, listOf("READY"))
                _readyOrders.value = result
                _text.value = "Found ${result.size} ready orders"
            } catch (e: Exception) {
                _text.value = "Error: ${e.message}"
            }
        }
    }

    private fun subscribeToUpdates() {
        viewModelScope.launch {
            val restaurantId = NetworkModule.getTokenManager().getRestaurantId()
            NetworkModule.realtimeService.subscribeToOrders(restaurantId).collect {
                // When any event comes, refresh the list
                fetchReadyOrders()
            }
        }
    }
}
