package com.cuea.rmp.mobile.ui.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class ProjectViewModel @Inject constructor(
    private val projectRepository: ProjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectUiState())
    val uiState: StateFlow<ProjectUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            projectRepository.observeProjects().collectLatest { list ->
                _uiState.update {
                    it.copy(
                        projects = list.map { entry ->
                            ProjectItemUi(
                                id = entry.id,
                                name = entry.name,
                                status = entry.status,
                                managerId = entry.managerId,
                                period = "${entry.startDate} to ${entry.endDate}",
                                description = entry.description.orEmpty()
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
                projectRepository.refreshProjects()
            }.onSuccess {
                _uiState.update { it.copy(isRefreshing = false) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        errorMessage = throwable.message ?: "Could not refresh projects"
                    )
                }
            }
        }
    }
}

data class ProjectUiState(
    val projects: List<ProjectItemUi> = emptyList(),
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

data class ProjectItemUi(
    val id: String,
    val name: String,
    val status: String,
    val managerId: String,
    val period: String,
    val description: String
)

