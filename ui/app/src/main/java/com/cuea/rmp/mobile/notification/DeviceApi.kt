package com.cuea.rmp.mobile.notification

import com.cuea.rmp.mobile.core.network.ApiResponse
import com.cuea.rmp.mobile.notification.dto.DeviceTokenResponse
import com.cuea.rmp.mobile.notification.dto.RegisterDeviceTokenRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface DeviceApi {

    @POST("api/v1/devices/token")
    suspend fun registerDeviceToken(@Body request: RegisterDeviceTokenRequest): ApiResponse<DeviceTokenResponse>
}

