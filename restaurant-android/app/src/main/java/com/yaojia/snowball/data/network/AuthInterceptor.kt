package com.yaojia.snowball.data.network

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.OkHttpClient

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
            // Avoid infinite loops for login or refresh endpoints
            if (originalRequest.url.encodedPath.contains("/login") || 
                originalRequest.url.encodedPath.contains("/refresh")) {
                return response
            }

            synchronized(this) {
                // Check if token has changed (another thread might have refreshed it)
                val currentToken = tokenManager.getToken()
                if (currentToken != null && currentToken != token) {
                    response.close()
                    val newRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer $currentToken")
                        .build()
                    return chain.proceed(newRequest)
                }

                val refreshToken = tokenManager.getRefreshToken() ?: return response

                try {
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

                    if (refreshResponse.isSuccessful) {
                        val responseBody = refreshResponse.body?.string()
                        val newToken = extractToken(responseBody, "token")
                        val newRefreshToken = extractToken(responseBody, "refreshToken")

                        if (newToken != null) {
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
                    }
                    refreshResponse.close()
                } catch (e: Exception) {
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
