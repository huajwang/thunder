package com.yaojia.snowball.data.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    // TODO: Replace with your actual server IP if running on a real device.
    // 10.0.2.2 is the localhost alias for the Android Emulator.
    // For physical device, use your computer's local IP (e.g., 10.0.0.212)
    private const val BASE_URL = "http://10.0.0.212:8080/"

    private lateinit var tokenManager: TokenManager

    fun init(context: Context) {
        tokenManager = TokenManager(context)
    }

    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor(AuthInterceptor(tokenManager))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val apiService: RestaurantApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RestaurantApiService::class.java)
    }

    val realtimeService: RealtimeService by lazy {
        RealtimeService(client, BASE_URL, tokenManager)
    }
    
    fun getTokenManager(): TokenManager = tokenManager
}
