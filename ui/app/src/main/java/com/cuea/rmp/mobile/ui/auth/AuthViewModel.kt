package com.cuea.rmp.mobile.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuea.rmp.mobile.auth.AuthRepository
import com.cuea.rmp.mobile.auth.dto.LoginRequest
import com.cuea.rmp.mobile.auth.dto.RegisterRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onIsRegisterChanged(value: Boolean) {
        _uiState.update { it.copy(isRegister = value, errorMessage = null) }
    }

    fun onOrgIdChanged(value: String) {
        _uiState.update { it.copy(orgId = value, errorMessage = null) }
    }

    fun onFullNameChanged(value: String) {
        _uiState.update { it.copy(fullName = value, errorMessage = null) }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching {
                if (state.isRegister) {
                    authRepository.register(
                        RegisterRequest(
                            orgId = state.orgId.trim(),
                            fullName = state.fullName.trim(),
                            email = state.email.trim(),
                            password = state.password
                        )
                    )
                    authRepository.login(
                        LoginRequest(
                            email = state.email.trim(),
                            password = state.password
                        )
                    )
                } else {
                    authRepository.login(
                        LoginRequest(
                            email = state.email.trim(),
                            password = state.password
                        )
                    )
                }
            }.onSuccess {
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = null,
                        password = ""
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Authentication failed"
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            runCatching { authRepository.logout() }
            _uiState.update {
                it.copy(
                    password = "",
                    errorMessage = null,
                    isLoading = false
                )
            }
        }
    }
}

data class AuthUiState(
    val isRegister: Boolean = false,
    val orgId: String = "",
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

