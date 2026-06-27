package com.cuea.rmp.mobile.ui.project

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuea.rmp.mobile.auth.TokenManager
import com.cuea.rmp.mobile.project.AssignmentRepository
import com.cuea.rmp.mobile.project.ProjectRepository
import com.cuea.rmp.mobile.sync.ConflictUi
import com.cuea.rmp.mobile.sync.SyncFailureUi
import com.cuea.rmp.mobile.sync.SyncRepository
import com.cuea.rmp.mobile.sync.SyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val EDIT_ROLES = setOf("ADMIN", "MANAGER")

@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val projectRepository: ProjectRepository,
    private val assignmentRepository: AssignmentRepository,
    private val syncRepository: SyncRepository,
    private val syncScheduler: SyncScheduler,
    private val tokenManager: TokenManager
) : ViewModel() {

    val projectId: String = checkNotNull(savedStateHandle["projectId"])
    private val localId = "PROJECT:$projectId"

    private val _uiState = MutableStateFlow(ProjectDetailUiState())
    val uiState: StateFlow<ProjectDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            projectRepository.observeProject(projectId).collectLatest { entity ->
                _uiState.update {
                    it.copy(
                        project = entity?.let { e ->
                            ProjectDetailUi(
                                name = e.name,
                                managerId = e.managerId,
                                status = e.status,
                                period = "${e.startDate} to ${e.endDate}",
                                description = e.description.orEmpty()
                            )
                        },
                        pendingEdit = entity?.pendingEdit ?: false,
                        editName = if (it.isEditing) it.editName else entity?.name ?: it.editName,
                        editDescription = if (it.isEditing) it.editDescription else entity?.description ?: it.editDescription,
                        editStartDate = if (it.isEditing) it.editStartDate else entity?.startDate ?: it.editStartDate,
                        editEndDate = if (it.isEditing) it.editEndDate else entity?.endDate ?: it.editEndDate,
                        editStatus = if (it.isEditing) it.editStatus else entity?.status ?: it.editStatus
                    )
                }
            }
        }

        viewModelScope.launch {
            assignmentRepository.observeAssignmentsForProject(projectId).collectLatest { list ->
                _uiState.update { it.copy(assignmentCount = list.size) }
            }
        }

        viewModelScope.launch {
            tokenManager.role.collectLatest { role ->
                _uiState.update { it.copy(canEdit = role in EDIT_ROLES) }
            }
        }

        viewModelScope.launch {
            syncRepository.observeConflicts().collectLatest { logs ->
                _uiState.update {
                    it.copy(
                        conflicts = logs.filter { log -> log.entityType == "PROJECT" && log.entityId == projectId }
                            .map { log -> ConflictUi(log.action, log.message, log.occurredAt) }
                    )
                }
            }
        }

        viewModelScope.launch {
            syncRepository.observeSyncFailures().collectLatest { failures ->
                _uiState.update {
                    it.copy(syncFailure = failures.firstOrNull { f -> f.localId == localId })
                }
            }
        }

        refresh()
    }

    fun retrySync() {
        viewModelScope.launch { runCatching { syncRepository.pushMutation(localId) } }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            // Push any queued edit before pulling fresh data — see
            // ResourceDetailViewModel.refresh for why (confirmed live: a manual refresh
            // otherwise silently overwrites a locally-pending edit with stale server data).
            runCatching { syncRepository.pushPendingMutations() }
            runCatching {
                projectRepository.refreshProject(projectId)
                assignmentRepository.refreshAssignmentsForProject(projectId)
                // Learns the current clientVersion basis for any edit queued before the
                // next pull — see ResourceDetailViewModel.refresh for the same rationale.
                syncRepository.refreshSyncMetadata()
            }.onSuccess {
                _uiState.update { it.copy(isRefreshing = false) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(isRefreshing = false, errorMessage = throwable.message ?: "Could not load project")
                }
            }
        }
    }

    fun startEdit() = _uiState.update { it.copy(isEditing = true, editError = null) }
    fun cancelEdit() = _uiState.update { it.copy(isEditing = false, editError = null) }

    fun onEditNameChanged(value: String) = _uiState.update { it.copy(editName = value) }
    fun onEditDescriptionChanged(value: String) = _uiState.update { it.copy(editDescription = value) }
    fun onEditStartDateChanged(value: String) = _uiState.update { it.copy(editStartDate = value) }
    fun onEditEndDateChanged(value: String) = _uiState.update { it.copy(editEndDate = value) }
    fun onEditStatusChanged(value: String) = _uiState.update { it.copy(editStatus = value) }

    fun saveEdit() {
        val state = _uiState.value
        if (state.editName.isBlank() || state.editStartDate.isBlank() || state.editEndDate.isBlank() || state.editStatus.isBlank()) {
            _uiState.update { it.copy(editError = "Enter a valid name, date range, and status") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, editError = null) }
            runCatching {
                projectRepository.editProjectOffline(
                    id = projectId,
                    name = state.editName.trim(),
                    description = state.editDescription.trim().ifBlank { null },
                    startDate = state.editStartDate.trim(),
                    endDate = state.editEndDate.trim(),
                    status = state.editStatus.trim()
                )
            }.onSuccess { queuedLocalId ->
                _uiState.update { it.copy(isSaving = false, isEditing = false) }
                // Targets just this mutation rather than "whatever's oldest in the queue"
                // (head-of-line blocking fix) — failures surface via observeSyncFailures()
                // above, not through editError, since the edit itself did queue correctly.
                runCatching { syncRepository.pushMutation(queuedLocalId) }
                syncScheduler.triggerImmediateSync()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(isSaving = false, editError = throwable.message ?: "Could not save the edit")
                }
            }
        }
    }
}

data class ProjectDetailUiState(
    val project: ProjectDetailUi? = null,
    val assignmentCount: Int = 0,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,

    val canEdit: Boolean = false,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val editError: String? = null,
    val pendingEdit: Boolean = false,
    val editName: String = "",
    val editDescription: String = "",
    val editStartDate: String = "",
    val editEndDate: String = "",
    val editStatus: String = "",
    val conflicts: List<ConflictUi> = emptyList(),
    val syncFailure: SyncFailureUi? = null
)

data class ProjectDetailUi(
    val name: String,
    val managerId: String,
    val status: String,
    val period: String,
    val description: String
)
