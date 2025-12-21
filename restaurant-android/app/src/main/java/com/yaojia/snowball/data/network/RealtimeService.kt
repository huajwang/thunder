package com.yaojia.snowball.data.network

import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class RealtimeService(
    private val client: OkHttpClient,
    private val baseUrl: String,
    private val tokenManager: TokenManager
) {

    fun subscribeToOrders(restaurantId: Long): Flow<String> = callbackFlow {
        Log.d("RealtimeService", "Starting subscription for restaurant $restaurantId")
        
        // Create a new client builder from the existing client
        val clientBuilder = client.newBuilder()
            .readTimeout(0, TimeUnit.MILLISECONDS)

        // Remove HttpLoggingInterceptor to prevent buffering of the SSE stream
        val interceptors = clientBuilder.interceptors()
        val loggingInterceptor = interceptors.find { it is HttpLoggingInterceptor }
        if (loggingInterceptor != null) {
            interceptors.remove(loggingInterceptor)
        }

        val sseClient = clientBuilder.build()

        val token = tokenManager.getToken()
        Log.d("RealtimeService", "Using token: ${token?.take(10)}...")
        
        val request = Request.Builder()
            .url("${baseUrl}api/orders/stream?restaurantId=$restaurantId&token=$token")
            .header("Accept", "text/event-stream")
            .build()

        val listener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                Log.d("RealtimeService", "Connection opened")
            }

            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                Log.d("RealtimeService", "Event received: $data")
                trySend(data)
            }

            override fun onClosed(eventSource: EventSource) {
                Log.d("RealtimeService", "Connection closed")
                close()
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                Log.e("RealtimeService", "Connection failed: ${t?.message}", t)
                if (response != null) {
                    Log.e("RealtimeService", "Response code: ${response.code}")
                    if (response.code == 401) {
                        Log.e("RealtimeService", "Token expired. Clearing token.")
                        tokenManager.clearToken()
                        close(Exception("Unauthorized"))
                        return
                    }
                }
            }
        }

        val factory = okhttp3.sse.EventSources.createFactory(sseClient)
        val eventSource = factory.newEventSource(request, listener)

        awaitClose {
            eventSource.cancel()
        }
    }
}
