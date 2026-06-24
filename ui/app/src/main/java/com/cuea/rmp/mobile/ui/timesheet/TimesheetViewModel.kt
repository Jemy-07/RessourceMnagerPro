package com.cuea.rmp.mobile.ui.timesheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuea.rmp.mobile.sync.SyncScheduler
import com.cuea.rmp.mobile.timesheet.LocalSyncState
import com.cuea.rmp.mobile.timesheet.TimesheetLocalEntity
import com.cuea.rmp.mobile.timesheet.TimesheetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TimesheetViewModel @Inject constructor(
    private val timesheetRepository: TimesheetRepository,
    private val syncScheduler: SyncScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimesheetUiState())
    val uiState: StateFlow<TimesheetUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            timesheetRepository.observeLocalTimesheets().collectLatest { entries ->
                _uiState.update { it.copy(entries = entries.map { entry -> entry.toUi() }) }
            }
        }
    }

    fun onResourceIdChanged(value: String) {
        _uiState.update { it.copy(resourceId = value, message = null) }
    }

    fun onAssignmentIdChanged(value: String) {
        _uiState.update { it.copy(assignmentId = value, message = null) }
    }

    fun onWorkDateChanged(value: String) {
        _uiState.update { it.copy(workDate = value, message = null) }
    }

    fun onHoursChanged(value: String) {
        _uiState.update { it.copy(hours = value, message = null) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }

            runCatching {
                timesheetRepository.logTime(
                    resourceId = state.resourceId.trim(),
                    assignmentId = state.assignmentId.trim(),
                    workDate = kotlinx.datetime.LocalDate.parse(state.workDate.trim()),
                    hours = state.hours.trim().toDouble()
                )
            }.onSuccess { id ->
                syncScheduler.triggerImmediateSync()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        hours = "",
                        message = "Queued timesheet $id for sync"
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = throwable.message ?: "Could not save timesheet"
                    )
                }
            }
        }
    }

    fun syncNow() {
        syncScheduler.triggerImmediateSync()
        _uiState.update { it.copy(message = "Sync requested") }
    }
}

data class TimesheetUiState(
    val resourceId: String = "",
    val assignmentId: String = "",
    val workDate: String = "",
    val hours: String = "",
    val entries: List<TimesheetEntryUi> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null
)

data class TimesheetEntryUi(
    val id: String,
    val resourceId: String,
    val assignmentId: String,
    val workDate: String,
    val hours: String,
    val syncState: String
)

private fun TimesheetLocalEntity.toUi(): TimesheetEntryUi {
    val syncLabel = when (syncState) {
        LocalSyncState.PENDING_SYNC -> "PENDING"
        LocalSyncState.SYNCED -> "SYNCED"
        LocalSyncState.FAILED -> "FAILED"
    }

    return TimesheetEntryUi(
        id = id,
        resourceId = resourceId,
        assignmentId = assignmentId,
        workDate = workDate.toString(),
        hours = hours.toString(),
        syncState = syncLabel
    )
}

