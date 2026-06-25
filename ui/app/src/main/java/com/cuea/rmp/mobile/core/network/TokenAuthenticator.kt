package com.cuea.rmp.mobile.core.network

import com.cuea.rmp.mobile.auth.RefreshAuthApi
import com.cuea.rmp.mobile.auth.TokenManager
import com.cuea.rmp.mobile.auth.dto.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val refreshAuthApi: RefreshAuthApi
) : Authenticator {

    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) {
            return null
        }

        synchronized(lock) {
            val currentAccess = runBlocking { tokenManager.getAccessToken() }
            val failedRequestAccess = response.request.header("Authorization")
                ?.removePrefix("Bearer ")
                ?.trim()

            // If another request already refreshed the token, just retry with the latest token.
            if (!currentAccess.isNullOrBlank() && currentAccess != failedRequestAccess) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentAccess")
                    .build()
            }

            val refreshToken = runBlocking { tokenManager.getRefreshToken() } ?: return null
            val refreshCall = refreshAuthApi.refreshBlocking(RefreshTokenRequest(refreshToken))
            val refreshResponse = refreshCall.execute()

            val refreshBody = refreshResponse.body()
            val authData = refreshBody?.data
            if (!refreshResponse.isSuccessful || refreshBody?.success != true || authData == null) {
                runBlocking { tokenManager.clearTokens() }
                return null
            }

            runBlocking {
                tokenManager.saveTokens(authData.accessToken, authData.refreshToken)
            }

            return response.request.newBuilder()
                .header("Authorization", "Bearer ${authData.accessToken}")
                .build()
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            count += 1
            priorResponse = priorResponse.priorResponse
        }
        return count
    }
}

