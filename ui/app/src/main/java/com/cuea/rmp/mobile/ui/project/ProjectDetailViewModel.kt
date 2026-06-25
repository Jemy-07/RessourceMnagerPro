package com.cuea.rmp.mobile.ui.project

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuea.rmp.mobile.project.AssignmentRepository
import com.cuea.rmp.mobile.project.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val projectRepository: ProjectRepository,
    private val assignmentRepository: AssignmentRepository
) : ViewModel() {

    val projectId: String = checkNotNull(savedStateHandle["projectId"])

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
                        }
                    )
                }
            }
        }

        viewModelScope.launch {
            assignmentRepository.observeAssignmentsForProject(projectId).collectLatest { list ->
                _uiState.update { it.copy(assignmentCount = list.size) }
            }
        }

        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            runCatching {
                projectRepository.refreshProject(projectId)
                assignmentRepository.refreshAssignmentsForProject(projectId)
            }.onSuccess {
                _uiState.update { it.copy(isRefreshing = false) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(isRefreshing = false, errorMessage = throwable.message ?: "Could not load project")
                }
            }
        }
    }
}

data class ProjectDetailUiState(
    val project: ProjectDetailUi? = null,
    val assignmentCount: Int = 0,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

data class ProjectDetailUi(
    val name: String,
    val managerId: String,
    val status: String,
    val period: String,
    val description: String
)
