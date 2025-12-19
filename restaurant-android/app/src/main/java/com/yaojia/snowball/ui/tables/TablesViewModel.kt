package com.yaojia.snowball.ui.tables

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yaojia.snowball.data.model.Table
import com.yaojia.snowball.data.network.NetworkModule
import kotlinx.coroutines.launch

class TablesViewModel : ViewModel() {

    private val _tables = MutableLiveData<List<Table>>()
    val tables: LiveData<List<Table>> = _tables

    private val _text = MutableLiveData<String>().apply {
        value = "Loading tables..."
    }
    val text: LiveData<String> = _text

    init {
        fetchTables()
    }

    fun fetchTables() {
        viewModelScope.launch {
            try {
                val restaurantId = NetworkModule.getTokenManager().getRestaurantId()
                val result = NetworkModule.apiService.getTables(restaurantId)
                _tables.value = result
                _text.value = "Found ${result.size} tables"
            } catch (e: Exception) {
                _text.value = "Error: ${e.message}"
            }
        }
    }
}
