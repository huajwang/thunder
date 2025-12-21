package com.yaojia.snowball.data.network

import com.yaojia.snowball.data.model.AuthRequest
import com.yaojia.snowball.data.model.AuthResponse
import com.yaojia.snowball.data.model.RefreshTokenRequest
import com.yaojia.snowball.data.model.Order
import com.yaojia.snowball.data.model.Table
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface RestaurantApiService {

    @POST("/api/auth/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    @POST("/api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): AuthResponse

    @GET("/api/orders")
    suspend fun getOrders(
        @Query("restaurantId") restaurantId: Long,
        @Query("statuses") statuses: List<String>? = null
    ): List<Order>

    @PUT("/api/orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") orderId: Long,
        @Body status: Map<String, String> // { "status": "PREPARING" }
    )

    @GET("/api/tables")
    suspend fun getTables(
        @Query("restaurantId") restaurantId: Long
    ): List<Table>

    @GET("/api/tables/{tableId}/bill")
    suspend fun getTableBill(
        @Path("tableId") tableId: Long
    ): List<Order>
}
