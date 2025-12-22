package com.yaojia.snowball.data.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

enum class LogoutReason {
    USER_REQUEST,
    SESSION_EXPIRED
}

class TokenManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val _tokenState = MutableStateFlow<String?>(prefs.getString("jwt_token", null))
    val tokenState: StateFlow<String?> = _tokenState.asStateFlow()

    private val _logoutEvent = MutableSharedFlow<LogoutReason>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val logoutEvent: SharedFlow<LogoutReason> = _logoutEvent.asSharedFlow()

    fun saveToken(token: String) {
        Log.d("TokenManager", "Saving new token: ${token.take(10)}...")
        prefs.edit().putString("jwt_token", token).apply()
        _tokenState.value = token
    }

    fun saveRefreshToken(token: String) {
        Log.d("TokenManager", "Saving new refresh token: ${token.take(10)}...")
        prefs.edit().putString("refresh_token", token).apply()
    }

    fun getToken(): String? {
        return prefs.getString("jwt_token", null)
    }

    fun getRefreshToken(): String? {
        return prefs.getString("refresh_token", null)
    }

    fun clearToken(reason: LogoutReason = LogoutReason.USER_REQUEST) {
        Log.d("TokenManager", "Clearing tokens. Reason: $reason")
        prefs.edit().remove("jwt_token").remove("refresh_token").apply()
        _tokenState.value = null
        _logoutEvent.tryEmit(reason)
    }
    
    fun saveRestaurantId(id: Long) {
        prefs.edit().putLong("restaurant_id", id).apply()
    }
    
    fun getRestaurantId(): Long {
        return prefs.getLong("restaurant_id", -1)
    }
}
