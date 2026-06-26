package com.cuea.rmp.mobile.auth

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore by preferencesDataStore(name = "auth_tokens")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val AccessToken: Preferences.Key<String> = stringPreferencesKey("access_token")
        val RefreshToken: Preferences.Key<String> = stringPreferencesKey("refresh_token")
    }

    private val preferences: Flow<Preferences> = context.authDataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }

    val accessToken: Flow<String?> = preferences.map { prefs -> prefs[Keys.AccessToken] }
    val refreshToken: Flow<String?> = preferences.map { prefs -> prefs[Keys.RefreshToken] }
    val role: Flow<String?> = accessToken.map { token -> token?.let(JwtUtils::extractRole) }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.authDataStore.edit { prefs ->
            prefs[Keys.AccessToken] = accessToken
            prefs[Keys.RefreshToken] = refreshToken
        }
    }

    suspend fun clearTokens() {
        context.authDataStore.edit { prefs ->
            prefs.remove(Keys.AccessToken)
            prefs.remove(Keys.RefreshToken)
        }
    }

    suspend fun getAccessToken(): String? = accessToken.first()

    suspend fun getRefreshToken(): String? = refreshToken.first()

    suspend fun getCurrentUserId(): String? = getAccessToken()?.let(JwtUtils::extractUserId)

    suspend fun getCurrentRole(): String? = getAccessToken()?.let(JwtUtils::extractRole)
}

