package com.cuea.rmp.mobile.core.network

import com.cuea.rmp.mobile.auth.OfflineTestLogin
import com.cuea.rmp.mobile.auth.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = runBlocking { tokenManager.accessToken.first() }

        if (accessToken == OfflineTestLogin.sentinelAccessToken) {
            throw OfflineTestSessionException()
        }

        val requestBuilder = chain.request().newBuilder()

        if (!accessToken.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $accessToken")
        }

        return chain.proceed(requestBuilder.build())
    }
}

