package com.yaojia.snowball.data.network

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.OkHttpClient
import android.util.Log

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenManager.getToken()
        
        val requestBuilder = originalRequest.newBuilder()
        if (token != null) {
            requestBuilder.header("Authorization", "Bearer $token")
        }
        
        val response = chain.proceed(requestBuilder.build())

        if (response.code == 401) {
            Log.d("AuthInterceptor", "Received 401 for ${originalRequest.url}")
            // Avoid infinite loops for login or refresh endpoints
            if (originalRequest.url.encodedPath.contains("/login") || 
                originalRequest.url.encodedPath.contains("/refresh")) {
                Log.d("AuthInterceptor", "401 on login/refresh endpoint, returning response")
                return response
            }

            synchronized(this) {
                // Check if token has changed (another thread might have refreshed it)
                val currentToken = tokenManager.getToken()
                if (currentToken != null && currentToken != token) {
                    Log.d("AuthInterceptor", "Token already refreshed by another thread, retrying")
                    response.close()
                    val newRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer $currentToken")
                        .build()
                    return chain.proceed(newRequest)
                }

                val refreshToken = tokenManager.getRefreshToken()
                if (refreshToken == null) {
                    Log.d("AuthInterceptor", "No refresh token available")
                    return response
                }

                try {
                    Log.d("AuthInterceptor", "Attempting to refresh token")
                    // Construct refresh request
                    val refreshUrl = originalRequest.url.newBuilder()
                        .encodedPath("/api/auth/refresh")
                        .query(null)
                        .build()

                    val json = "{\"refreshToken\":\"$refreshToken\"}"
                    val body = json.toRequestBody("application/json".toMediaType())
                    
                    val refreshRequest = okhttp3.Request.Builder()
                        .url(refreshUrl)
                        .post(body)
                        .build()

                    // Use a new client to avoid sharing interceptors/state
                    val refreshClient = OkHttpClient()
                    val refreshResponse = refreshClient.newCall(refreshRequest).execute()

                    Log.d("AuthInterceptor", "Refresh response code: ${refreshResponse.code}")

                    if (refreshResponse.isSuccessful) {
                        val responseBody = refreshResponse.body?.string()
                        val newToken = extractToken(responseBody, "token")
                        val newRefreshToken = extractToken(responseBody, "refreshToken")

                        if (newToken != null) {
                            Log.d("AuthInterceptor", "Token refresh successful")
                            tokenManager.saveToken(newToken)
                            if (newRefreshToken != null) {
                                tokenManager.saveRefreshToken(newRefreshToken)
                            }
                            
                            refreshResponse.close()
                            response.close()

                            val retryRequest = originalRequest.newBuilder()
                                .header("Authorization", "Bearer $newToken")
                                .build()
                            return chain.proceed(retryRequest)
                        }
                    } else {
                        Log.e("AuthInterceptor", "Token refresh failed with code ${refreshResponse.code}")
                        // If refresh failed (e.g. 401), we should clear tokens to trigger logout
                        if (refreshResponse.code == 401) {
                            Log.e("AuthInterceptor", "Refresh token expired or invalid. Clearing tokens.")
                            tokenManager.clearToken(LogoutReason.SESSION_EXPIRED)
                        }
                    }
                    refreshResponse.close()
                } catch (e: Exception) {
                    Log.e("AuthInterceptor", "Exception during token refresh", e)
                    e.printStackTrace()
                }
            }
        }

        return response
    }

    private fun extractToken(json: String?, key: String): String? {
        if (json == null) return null
        val pattern = "\"$key\":\"(.*?)\""
        val regex = Regex(pattern)
        val match = regex.find(json)
        return match?.groupValues?.get(1)
    }
}
