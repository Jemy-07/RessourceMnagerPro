package com.cuea.rmp.mobile.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuea.rmp.mobile.notification.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            notificationRepository.observeNotifications().collectLatest { list ->
                _uiState.update {
                    it.copy(
                        notifications = list.map { entry ->
                            NotificationItemUi(
                                id = entry.id,
                                type = entry.type,
                                message = entry.message,
                                read = entry.read
                            )
                        }
                    )
                }
            }
        }

        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            runCatching {
                notificationRepository.refreshNotifications()
            }.onSuccess {
                _uiState.update { it.copy(isRefreshing = false) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        errorMessage = throwable.message ?: "Could not refresh notifications"
                    )
                }
            }
        }
    }

    fun markRead(id: String) {
        viewModelScope.launch {
            runCatching { notificationRepository.markRead(id) }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(errorMessage = throwable.message ?: "Could not mark notification as read")
                    }
                }
        }
    }
}

data class NotificationUiState(
    val notifications: List<NotificationItemUi> = emptyList(),
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

data class NotificationItemUi(
    val id: String,
    val type: String,
    val message: String,
    val read: Boolean
)

