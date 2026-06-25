package com.cuea.rmp.mobile.notification

import com.cuea.rmp.mobile.core.network.ApiResponse
import com.cuea.rmp.mobile.notification.dto.NotificationResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface NotificationApi {

    @GET("api/v1/notifications")
    suspend fun listNotifications(): ApiResponse<List<NotificationResponse>>

    @POST("api/v1/notifications/{id}/read")
    suspend fun markRead(@Path("id") id: String): ApiResponse<NotificationResponse>
}

