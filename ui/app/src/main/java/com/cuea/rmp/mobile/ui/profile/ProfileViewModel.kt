package com.cuea.rmp.mobile.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuea.rmp.mobile.auth.TokenManager
import com.cuea.rmp.mobile.user.UserRepository
import com.cuea.rmp.mobile.user.dto.UpdateUserRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Backend gap (separately tracked, not fixed here): UserController is
 * `@PreAuthorize("hasRole('ADMIN')")` at the class level, so a non-admin user calling
 * GET/PUT on their own id gets HTTP 403. This screen will only actually work end-to-end
 * for an ADMIN-role test account until the backend adds a self-service exemption.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var userId: String? = null

    init {
        viewModelScope.launch {
            val id = tokenManager.getCurrentUserId()
            userId = id
            if (id == null) {
                _uiState.update { it.copy(errorMessage = "Could not identify the logged-in user") }
                return@launch
            }

            launch {
                userRepository.observeUser(id).collectLatest { entity ->
                    if (entity != null) {
                        _uiState.update {
                            it.copy(
                                fullName = entity.fullName,
                                email = entity.email,
                                role = entity.role,
                                active = entity.active
                            )
                        }
                    }
                }
            }

            refresh()
        }
    }

    fun refresh() {
        val id = userId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            runCatching {
                userRepository.refreshUser(id)
            }.onSuccess {
                _uiState.update { it.copy(isRefreshing = false) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(isRefreshing = false, errorMessage = throwable.message ?: "Could not load profile")
                }
            }
        }
    }

    fun onFullNameChanged(value: String) = _uiState.update { it.copy(fullName = value) }

    fun save() {
        val id = userId ?: return
        val state = _uiState.value
        if (state.fullName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Name cannot be blank") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                userRepository.updateUser(id, UpdateUserRequest(fullName = state.fullName, role = state.role))
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = throwable.message ?: "Could not save profile")
                }
            }
        }
    }
}

data class ProfileUiState(
    val fullName: String = "",
    val email: String = "",
    val role: String = "",
    val active: Boolean = true,
    val isRefreshing: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)
