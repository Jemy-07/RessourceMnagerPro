package com.cuea.rmp.mobile.auth

import com.cuea.rmp.mobile.auth.dto.AuthResponse
import com.cuea.rmp.mobile.auth.dto.LoginRequest
import com.cuea.rmp.mobile.auth.dto.LogoutRequest
import com.cuea.rmp.mobile.auth.dto.RefreshTokenRequest
import com.cuea.rmp.mobile.auth.dto.RegisterRequest
import com.cuea.rmp.mobile.auth.dto.RegisteredUserResponse
import com.cuea.rmp.mobile.core.network.ApiResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<RegisteredUserResponse>

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<AuthResponse>

    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequest): ApiResponse<AuthResponse>

    @POST("api/v1/auth/logout")
    suspend fun logout(@Body request: LogoutRequest): ApiResponse<Unit>
}

