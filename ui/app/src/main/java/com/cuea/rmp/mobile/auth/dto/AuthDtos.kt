package com.cuea.rmp.mobile.auth.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val orgId: String,
    val fullName: String,
    val email: String,
    val password: String
)

@Serializable
data class RegisteredUserResponse(
    val userId: String,
    val email: String,
    val role: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class LogoutRequest(
    val refreshToken: String
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Long
)

