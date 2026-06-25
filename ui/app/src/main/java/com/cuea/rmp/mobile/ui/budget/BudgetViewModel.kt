package com.cuea.rmp.mobile.ui.budget

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuea.rmp.mobile.budget.BudgetRepository
import com.cuea.rmp.mobile.budget.dto.AllocateBudgetRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class BudgetViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val projectId: String = checkNotNull(savedStateHandle["projectId"])

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            budgetRepository.observeBudgetForProject(projectId).collectLatest { entity ->
                _uiState.update {
                    it.copy(
                        budget = entity?.let { e ->
                            // margin/remaining come straight from the backend's real
                            // calculation (Budget.margin()/remaining()) — displayed as-is.
                            BudgetSummaryUi(
                                currency = e.currency,
                                totalAmount = e.totalAmount,
                                allocatedAmount = e.allocatedAmount,
                                spentAmount = e.spentAmount,
                                margin = e.margin,
                                remaining = e.remaining
                            )
                        },
                        allocationTotal = entity?.totalAmount?.toString() ?: it.allocationTotal,
                        allocationAllocated = entity?.allocatedAmount?.toString() ?: it.allocationAllocated,
                        allocationCurrency = entity?.currency ?: it.allocationCurrency
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
                budgetRepository.refreshBudget(projectId)
            }.onSuccess {
                _uiState.update { it.copy(isRefreshing = false) }
            }.onFailure { throwable ->
                _uiState.update {
                    // No budget allocated yet is an expected state, not a hard failure —
                    // still surface it so the allocation form is the obvious next step.
                    it.copy(isRefreshing = false, errorMessage = throwable.message ?: "No budget found for this project yet")
                }
            }
        }
    }

    fun onAllocationTotalChanged(value: String) = _uiState.update { it.copy(allocationTotal = value) }
    fun onAllocationAllocatedChanged(value: String) = _uiState.update { it.copy(allocationAllocated = value) }
    fun onAllocationCurrencyChanged(value: String) = _uiState.update { it.copy(allocationCurrency = value) }

    fun submitAllocation() {
        val state = _uiState.value
        val total = state.allocationTotal.toDoubleOrNull()
        val allocated = state.allocationAllocated.toDoubleOrNull()

        if (total == null || allocated == null || state.allocationCurrency.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter valid total/allocated amounts and a currency") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                budgetRepository.allocateBudget(
                    projectId = projectId,
                    request = AllocateBudgetRequest(
                        totalAmount = total,
                        allocatedAmount = allocated,
                        currency = state.allocationCurrency
                    )
                )
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = throwable.message ?: "Could not allocate budget")
                }
            }
        }
    }
}

data class BudgetUiState(
    val budget: BudgetSummaryUi? = null,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,

    val allocationTotal: String = "",
    val allocationAllocated: String = "",
    val allocationCurrency: String = "",
    val isSaving: Boolean = false
)

data class BudgetSummaryUi(
    val currency: String,
    val totalAmount: Double,
    val allocatedAmount: Double,
    val spentAmount: Double,
    val margin: Double,
    val remaining: Double
)
