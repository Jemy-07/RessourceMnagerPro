package com.cuea.rmp.mobile.ui.resource

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuea.rmp.mobile.resource.ResourceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ResourceViewModel @Inject constructor(
    private val resourceRepository: ResourceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResourceUiState())
    val uiState: StateFlow<ResourceUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            resourceRepository.observeResources().collectLatest { list ->
                _uiState.update {
                    it.copy(
                        resources = list.map { entry ->
                            ResourceItemUi(
                                id = entry.id,
                                name = entry.name,
                                type = entry.type,
                                rate = "${entry.hourlyRateAmount} ${entry.currency}",
                                availability = entry.availabilityStatus,
                                skillsSummary = entry.skillsSummary
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
                resourceRepository.refreshResources()
            }.onSuccess {
                _uiState.update { it.copy(isRefreshing = false) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        errorMessage = throwable.message ?: "Could not refresh resources"
                    )
                }
            }
        }
    }
}

data class ResourceUiState(
    val resources: List<ResourceItemUi> = emptyList(),
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

data class ResourceItemUi(
    val id: String,
    val name: String,
    val type: String,
    val rate: String,
    val availability: String,
    val skillsSummary: String
)

