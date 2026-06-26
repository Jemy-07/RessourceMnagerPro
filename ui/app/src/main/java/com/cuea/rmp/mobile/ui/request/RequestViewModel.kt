package com.cuea.rmp.mobile.ui.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuea.rmp.mobile.auth.TokenManager
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
    private val requestRepository: RequestRepository,
    private val tokenManager: TokenManager
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

    fun onNewResourceIdChanged(value: String) = _uiState.update { it.copy(newResourceId = value) }
    fun onNewProjectIdChanged(value: String) = _uiState.update { it.copy(newProjectId = value) }
    fun onNewTitleChanged(value: String) = _uiState.update { it.copy(newTitle = value) }
    fun onNewStartDateChanged(value: String) = _uiState.update { it.copy(newStartDate = value) }
    fun onNewEndDateChanged(value: String) = _uiState.update { it.copy(newEndDate = value) }
    fun onNewAllocationPctChanged(value: String) = _uiState.update { it.copy(newAllocationPct = value) }

    // Visible to every role (RequestController.create is isAuthenticated()-only, unlike
    // Resource/Project's ADMIN/MANAGER-gated writes) — works offline via the same
    // REST-replay queue Timesheet uses, since a create has nothing to conflict against.
    fun createRequest() {
        val state = _uiState.value
        val allocationPct = state.newAllocationPct.toIntOrNull()
        if (state.newResourceId.isBlank() || state.newProjectId.isBlank() || state.newTitle.isBlank() ||
            state.newStartDate.isBlank() || state.newEndDate.isBlank() || allocationPct == null
        ) {
            _uiState.update { it.copy(errorMessage = "Fill in all fields with a valid allocation %") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, errorMessage = null) }
            runCatching {
                requestRepository.createRequestOffline(
                    resourceId = state.newResourceId.trim(),
                    projectId = state.newProjectId.trim(),
                    title = state.newTitle.trim(),
                    startDate = state.newStartDate.trim(),
                    endDate = state.newEndDate.trim(),
                    allocationPct = allocationPct,
                    requesterId = tokenManager.getCurrentUserId()
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isCreating = false,
                        newResourceId = "",
                        newProjectId = "",
                        newTitle = "",
                        newStartDate = "",
                        newEndDate = "",
                        newAllocationPct = ""
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(isCreating = false, errorMessage = throwable.message ?: "Could not create request")
                }
            }
        }
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
    val errorMessage: String? = null,

    val newResourceId: String = "",
    val newProjectId: String = "",
    val newTitle: String = "",
    val newStartDate: String = "",
    val newEndDate: String = "",
    val newAllocationPct: String = "",
    val isCreating: Boolean = false
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

