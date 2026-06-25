package com.cuea.rmp.mobile.auth

import com.cuea.rmp.mobile.auth.dto.AuthResponse
import com.cuea.rmp.mobile.auth.dto.RefreshTokenRequest
import com.cuea.rmp.mobile.core.network.ApiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface RefreshAuthApi {

    @POST("api/v1/auth/refresh")
    fun refreshBlocking(@Body request: RefreshTokenRequest): Call<ApiResponse<AuthResponse>>
}

