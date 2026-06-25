package com.cuea.rmp.mobile.auth

import com.cuea.rmp.mobile.auth.dto.AuthResponse
import com.cuea.rmp.mobile.auth.dto.LoginRequest
import com.cuea.rmp.mobile.auth.dto.LogoutRequest
import com.cuea.rmp.mobile.auth.dto.RefreshTokenRequest
import com.cuea.rmp.mobile.auth.dto.RegisterRequest
import com.cuea.rmp.mobile.auth.dto.RegisteredUserResponse
import com.cuea.rmp.mobile.core.network.safeApiCall
import com.cuea.rmp.mobile.core.network.safeApiCallUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
    private val json: Json
) {

    suspend fun register(request: RegisterRequest): RegisteredUserResponse {
        return safeApiCall(json) { authApi.register(request) }
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        if (matchesOfflineTestCredentials(request)) {
            val authResponse = AuthResponse(
                accessToken = "debug-access-token",
                refreshToken = "debug-refresh-token",
                tokenType = "Bearer",
                expiresIn = 15 * 60L
            )
            tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)
            return authResponse
        }

        val authResponse = safeApiCall(json) { authApi.login(request) }
        tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)
        return authResponse
    }

    suspend fun refresh(): AuthResponse {
        val refreshToken = tokenManager.getRefreshToken()
            ?: error("No refresh token available")

        val authResponse = safeApiCall(json) {
            authApi.refresh(RefreshTokenRequest(refreshToken))
        }

        tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)
        return authResponse
    }

    suspend fun logout() {
        val refreshToken = tokenManager.getRefreshToken()
        if (!refreshToken.isNullOrBlank()) {
            safeApiCallUnit(json) { authApi.logout(LogoutRequest(refreshToken)) }
        }
        tokenManager.clearTokens()
    }

    private fun matchesOfflineTestCredentials(request: LoginRequest): Boolean {
        if (!OfflineTestLogin.enabled) return false
        return request.email.trim().equals(OfflineTestLogin.email, ignoreCase = true) &&
            request.password == OfflineTestLogin.password
    }
}
