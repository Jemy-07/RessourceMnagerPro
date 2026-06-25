package com.cuea.rmp.mobile.ui.resource

import androidx.lifecycle.SavedStateHandle
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
class ResourceDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val resourceRepository: ResourceRepository
) : ViewModel() {

    private val resourceId: String = checkNotNull(savedStateHandle["resourceId"])

    private val _uiState = MutableStateFlow(ResourceDetailUiState())
    val uiState: StateFlow<ResourceDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            resourceRepository.observeResource(resourceId).collectLatest { entity ->
                _uiState.update {
                    it.copy(
                        resource = entity?.let { e ->
                            ResourceDetailUi(
                                name = e.name,
                                type = e.type,
                                rate = "${e.hourlyRateAmount} ${e.currency}",
                                availabilityStatus = e.availabilityStatus,
                                skillsSummary = e.skillsSummary
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
                resourceRepository.refreshResource(resourceId)
            }.onSuccess {
                _uiState.update { it.copy(isRefreshing = false) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(isRefreshing = false, errorMessage = throwable.message ?: "Could not load resource")
                }
            }
        }
    }

    fun onAvailabilityFromChanged(value: String) = _uiState.update { it.copy(availabilityFrom = value) }
    fun onAvailabilityToChanged(value: String) = _uiState.update { it.copy(availabilityTo = value) }

    // Closest available substitute for a time-off calendar — the backend exposes no
    // endpoint to list a resource's time-off directly (see ResourceRepository.checkAvailability).
    fun checkAvailability() {
        val state = _uiState.value
        if (state.availabilityFrom.isBlank() || state.availabilityTo.isBlank()) {
            _uiState.update { it.copy(availabilityError = "Enter a date range") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingAvailability = true, availabilityError = null) }
            runCatching {
                resourceRepository.checkAvailability(resourceId, state.availabilityFrom, state.availabilityTo)
            }.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        isCheckingAvailability = false,
                        availabilityResult = "${if (response.available) "Available" else "Not available"} — ${response.reason}"
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isCheckingAvailability = false,
                        availabilityError = throwable.message ?: "Could not check availability"
                    )
                }
            }
        }
    }
}

data class ResourceDetailUiState(
    val resource: ResourceDetailUi? = null,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,

    val availabilityFrom: String = "",
    val availabilityTo: String = "",
    val isCheckingAvailability: Boolean = false,
    val availabilityResult: String? = null,
    val availabilityError: String? = null
)

data class ResourceDetailUi(
    val name: String,
    val type: String,
    val rate: String,
    val availabilityStatus: String,
    val skillsSummary: String
)
