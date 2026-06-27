package com.cuea.rmp.mobile.ui.resource

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuea.rmp.mobile.auth.TokenManager
import com.cuea.rmp.mobile.resource.ResourceRepository
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
class ResourceDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val resourceRepository: ResourceRepository,
    private val syncRepository: SyncRepository,
    private val syncScheduler: SyncScheduler,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val resourceId: String = checkNotNull(savedStateHandle["resourceId"])
    private val localId = "RESOURCE:$resourceId"

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
                        },
                        pendingEdit = entity?.pendingEdit ?: false,
                        // Only seed the edit form fields while the user isn't actively
                        // mid-edit, so a background refresh can't clobber unsaved input.
                        editName = if (it.isEditing) it.editName else entity?.name ?: it.editName,
                        editRateAmount = if (it.isEditing) it.editRateAmount else entity?.hourlyRateAmount?.toString() ?: it.editRateAmount,
                        editCurrency = if (it.isEditing) it.editCurrency else entity?.currency ?: it.editCurrency,
                        editAvailabilityStatus = if (it.isEditing) it.editAvailabilityStatus else entity?.availabilityStatus ?: it.editAvailabilityStatus
                    )
                }
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
                        conflicts = logs.filter { log -> log.entityType == "RESOURCE" && log.entityId == resourceId }
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
            // Push any queued edit before pulling fresh data — pulling first would
            // silently overwrite the locally-pending edit with stale server content
            // (confirmed live: tapping Refresh while a manager edit sat queued wiped
            // both the "pending sync" banner and the edited values off the screen).
            // Swallowed separately: a push failure (e.g. still offline) shouldn't block
            // the regular GET-based refresh below from at least trying.
            runCatching { syncRepository.pushPendingMutations() }
            runCatching {
                resourceRepository.refreshResource(resourceId)
                // Learns the current clientVersion basis for any edit queued before the
                // next pull — without this, editResourceOffline() falls back to whatever
                // version was last known (0 if never pulled).
                syncRepository.refreshSyncMetadata()
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

    fun startEdit() = _uiState.update { it.copy(isEditing = true, editError = null) }
    fun cancelEdit() = _uiState.update { it.copy(isEditing = false, editError = null) }

    fun onEditNameChanged(value: String) = _uiState.update { it.copy(editName = value) }
    fun onEditRateAmountChanged(value: String) = _uiState.update { it.copy(editRateAmount = value) }
    fun onEditCurrencyChanged(value: String) = _uiState.update { it.copy(editCurrency = value) }
    fun onEditAvailabilityStatusChanged(value: String) = _uiState.update { it.copy(editAvailabilityStatus = value) }

    fun saveEdit() {
        val state = _uiState.value
        val amount = state.editRateAmount.toDoubleOrNull()
        if (state.editName.isBlank() || amount == null || state.editCurrency.isBlank() || state.editAvailabilityStatus.isBlank()) {
            _uiState.update { it.copy(editError = "Enter a valid name, rate, currency, and availability status") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, editError = null) }
            runCatching {
                resourceRepository.editResourceOffline(
                    id = resourceId,
                    name = state.editName.trim(),
                    hourlyRateAmount = amount,
                    currency = state.editCurrency.trim(),
                    availabilityStatus = state.editAvailabilityStatus.trim()
                )
            }.onSuccess { queuedLocalId ->
                _uiState.update { it.copy(isSaving = false, isEditing = false) }
                // Targets just this mutation rather than "whatever's oldest in the queue"
                // (head-of-line blocking fix) — a push failure here doesn't mean the edit
                // was lost, just that it's not synced yet; observeSyncFailures() above
                // surfaces that separately rather than through editError.
                runCatching { syncRepository.pushMutation(queuedLocalId) }
                // Resilience fallback: still ask the worker to retry later (e.g. if the
                // process dies right after this call, or the device is genuinely offline).
                syncScheduler.triggerImmediateSync()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(isSaving = false, editError = throwable.message ?: "Could not save the edit")
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
    val availabilityError: String? = null,

    val canEdit: Boolean = false,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val editError: String? = null,
    val pendingEdit: Boolean = false,
    val editName: String = "",
    val editRateAmount: String = "",
    val editCurrency: String = "",
    val editAvailabilityStatus: String = "",
    val conflicts: List<ConflictUi> = emptyList(),
    val syncFailure: SyncFailureUi? = null
)

data class ResourceDetailUi(
    val name: String,
    val type: String,
    val rate: String,
    val availabilityStatus: String,
    val skillsSummary: String
)
