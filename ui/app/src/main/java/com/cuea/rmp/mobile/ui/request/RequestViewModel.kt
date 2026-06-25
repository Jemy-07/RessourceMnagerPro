package com.cuea.rmp.mobile.ui.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuea.rmp.mobile.request.RequestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RequestViewModel @Inject constructor(
    private val requestRepository: RequestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RequestUiState())
    val uiState: StateFlow<RequestUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            requestRepository.observeRequests().collectLatest { list ->
                _uiState.update {
                    it.copy(
                        requests = list.map { entry ->
                            RequestItemUi(
                                id = entry.id,
                                title = entry.title,
                                resourceId = entry.resourceId,
                                projectId = entry.projectId,
                                period = "${entry.startDate} to ${entry.endDate}",
                                allocationPct = entry.allocationPct,
                                status = entry.status,
                                comments = entry.comments.orEmpty()
                            )
                        }
                    )
                }
            }
        }

        refresh()
    }

    fun onRejectCommentChanged(value: String) {
        _uiState.update { it.copy(rejectComment = value) }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            runCatching {
                requestRepository.refreshRequests()
            }.onSuccess {
                _uiState.update { it.copy(isRefreshing = false) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        errorMessage = throwable.message ?: "Could not refresh requests"
                    )
                }
            }
        }
    }

    fun approve(id: String) {
        viewModelScope.launch {
            runCatching { requestRepository.approve(id) }
                .onSuccess { refresh() }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(errorMessage = throwable.message ?: "Could not approve request")
                    }
                }
        }
    }

    fun reject(id: String) {
        val comment = _uiState.value.rejectComment.trim()
        if (comment.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Reject comment is required") }
            return
        }

        viewModelScope.launch {
            runCatching { requestRepository.reject(id, comment) }
                .onSuccess {
                    _uiState.update { it.copy(rejectComment = "") }
                    refresh()
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(errorMessage = throwable.message ?: "Could not reject request")
                    }
                }
        }
    }
}

data class RequestUiState(
    val requests: List<RequestItemUi> = emptyList(),
    val rejectComment: String = "",
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

data class RequestItemUi(
    val id: String,
    val title: String,
    val resourceId: String,
    val projectId: String,
    val period: String,
    val allocationPct: Int,
    val status: String,
    val comments: String
)

