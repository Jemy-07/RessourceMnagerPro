package com.cuea.rmp.mobile.ui.project

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuea.rmp.mobile.project.AssignmentRepository
import com.cuea.rmp.mobile.project.dto.AssignResourceRequest
import com.cuea.rmp.mobile.project.dto.UpdateAssignmentRequest
import com.cuea.rmp.mobile.resource.ResourceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

@HiltViewModel
class AssignmentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val assignmentRepository: AssignmentRepository,
    private val resourceRepository: ResourceRepository
) : ViewModel() {

    private val projectId: String = checkNotNull(savedStateHandle["projectId"])

    private val _uiState = MutableStateFlow(AssignmentUiState())
    val uiState: StateFlow<AssignmentUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            assignmentRepository.observeAssignmentsForProject(projectId).collectLatest { list ->
                _uiState.update {
                    it.copy(
                        assignments = list.map { entry ->
                            AssignmentItemUi(
                                id = entry.id,
                                resourceId = entry.resourceId,
                                title = entry.title,
                                period = "${entry.startDate} to ${entry.endDate}",
                                allocationPct = entry.allocationPct,
                                status = entry.status
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
                assignmentRepository.refreshAssignmentsForProject(projectId)
            }.onSuccess {
                _uiState.update { it.copy(isRefreshing = false) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(isRefreshing = false, errorMessage = throwable.message ?: "Could not refresh assignments")
                }
            }
        }
    }

    fun startCreate() {
        _uiState.update {
            it.copy(
                editingId = null,
                title = "",
                resourceId = "",
                startDate = "",
                endDate = "",
                allocationPct = "",
                errorMessage = null
            )
        }
    }

    fun startEdit(item: AssignmentItemUi) {
        val (start, end) = item.period.split(" to ").let { it.getOrElse(0) { "" } to it.getOrElse(1) { "" } }
        _uiState.update {
            it.copy(
                editingId = item.id,
                title = item.title,
                resourceId = item.resourceId,
                startDate = start,
                endDate = end,
                allocationPct = item.allocationPct.toString(),
                errorMessage = null
            )
        }
    }

    fun onTitleChanged(value: String) = _uiState.update { it.copy(title = value) }
    fun onResourceIdChanged(value: String) = _uiState.update { it.copy(resourceId = value) }
    fun onStartDateChanged(value: String) = _uiState.update { it.copy(startDate = value) }
    fun onEndDateChanged(value: String) = _uiState.update { it.copy(endDate = value) }
    fun onAllocationPctChanged(value: String) = _uiState.update { it.copy(allocationPct = value) }

    fun submit() {
        val state = _uiState.value
        val allocation = state.allocationPct.toIntOrNull()
        val start = runCatching { LocalDate.parse(state.startDate) }.getOrNull()
        val end = runCatching { LocalDate.parse(state.endDate) }.getOrNull()

        if (state.resourceId.isBlank() || state.title.isBlank() || allocation == null || start == null || end == null) {
            _uiState.update { it.copy(errorMessage = "Fill in resource, title, dates (YYYY-MM-DD), and allocation %") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                val editingId = state.editingId
                if (editingId == null) {
                    assignmentRepository.createAssignment(
                        projectId = projectId,
                        request = AssignResourceRequest(
                            resourceId = state.resourceId,
                            title = state.title,
                            startDate = start,
                            endDate = end,
                            allocationPct = allocation
                        )
                    )
                } else {
                    assignmentRepository.updateAssignment(
                        id = editingId,
                        request = UpdateAssignmentRequest(
                            startDate = start,
                            endDate = end,
                            allocationPct = allocation
                        )
                    )
                }
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false) }
                startCreate()
                refresh()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = throwable.message ?: "Could not save assignment")
                }
            }
        }
    }

    fun onMatchSkillIdChanged(value: String) = _uiState.update { it.copy(matchSkillId = value) }
    fun onMatchFromChanged(value: String) = _uiState.update { it.copy(matchFrom = value) }
    fun onMatchToChanged(value: String) = _uiState.update { it.copy(matchTo = value) }

    fun searchMatches() {
        val state = _uiState.value
        if (state.matchSkillId.isBlank() || state.matchFrom.isBlank() || state.matchTo.isBlank()) {
            _uiState.update { it.copy(matchError = "Enter a skill id and date range to search") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isMatching = true, matchError = null) }
            runCatching {
                resourceRepository.matchResources(
                    skillId = state.matchSkillId,
                    from = state.matchFrom,
                    to = state.matchTo
                )
            }.onSuccess { results ->
                _uiState.update {
                    it.copy(
                        isMatching = false,
                        matchResults = results.map { match ->
                            MatchResultUi(
                                resourceId = match.resourceId,
                                name = match.name,
                                type = match.type,
                                proficiency = match.proficiency,
                                rate = "${match.hourlyRateAmount} ${match.currency}"
                            )
                        }
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(isMatching = false, matchError = throwable.message ?: "Could not find matching resources")
                }
            }
        }
    }

    fun selectMatch(resourceId: String) {
        _uiState.update { it.copy(resourceId = resourceId, matchResults = emptyList()) }
    }
}

data class AssignmentUiState(
    val assignments: List<AssignmentItemUi> = emptyList(),
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,

    val editingId: String? = null,
    val title: String = "",
    val resourceId: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val allocationPct: String = "",
    val isSaving: Boolean = false,

    val matchSkillId: String = "",
    val matchFrom: String = "",
    val matchTo: String = "",
    val matchResults: List<MatchResultUi> = emptyList(),
    val isMatching: Boolean = false,
    val matchError: String? = null
)

data class AssignmentItemUi(
    val id: String,
    val resourceId: String,
    val title: String,
    val period: String,
    val allocationPct: Int,
    val status: String
)

data class MatchResultUi(
    val resourceId: String,
    val name: String,
    val type: String,
    val proficiency: Int,
    val rate: String
)
