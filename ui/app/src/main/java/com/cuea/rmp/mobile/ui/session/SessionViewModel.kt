package com.cuea.rmp.mobile.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuea.rmp.mobile.auth.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    val isAuthenticated: StateFlow<Boolean> = tokenManager.accessToken
        .map { !it.isNullOrBlank() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    /** One-shot check used by the splash screen — reads the real DataStore value directly,
     * rather than [isAuthenticated]'s placeholder default before its first emission. */
    suspend fun awaitAuthenticationState(): Boolean =
        !tokenManager.accessToken.first().isNullOrBlank()
}

