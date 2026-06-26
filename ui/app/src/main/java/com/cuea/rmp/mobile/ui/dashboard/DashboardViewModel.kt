package com.cuea.rmp.mobile.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuea.rmp.mobile.core.network.ApiException
import com.cuea.rmp.mobile.notification.NotificationLocalEntity
import com.cuea.rmp.mobile.notification.NotificationRepository
import com.cuea.rmp.mobile.project.AssignmentLocalEntity
import com.cuea.rmp.mobile.project.AssignmentRepository
import com.cuea.rmp.mobile.reporting.ReportRepository
import com.cuea.rmp.mobile.reporting.dto.UtilizationRowResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val assignmentRepository: AssignmentRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            assignmentRepository.observeUpcomingAssignments(limit = 5).collectLatest { upcoming ->
                _uiState.update { it.copy(upcomingAssignments = upcoming) }
            }
        }
        viewModelScope.launch {
            notificationRepository.observeNotifications().collectLatest { notifications ->
                _uiState.update {
                    it.copy(
                        unreadAlertCount = notifications.count { n -> !n.read },
                        recentAlerts = notifications.take(3)
                    )
                }
            }
        }
        viewModelScope.launch {
            runCatching { notificationRepository.refreshNotifications() }
        }
        refreshReports()
    }

    fun refreshReports() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingReports = true, reportsErrorMessage = null, reportsForbidden = false) }
            runCatching {
                coroutineScope {
                    val cost = async { reportRepository.getCostReport() }
                    val utilization = async { reportRepository.getUtilizationReport() }
                    cost.await() to utilization.await()
                }
            }.onSuccess { (costRows, utilizationRows) ->
                // Cost amounts are summed assuming a single org-wide currency, true for the
                // seeded dev data — a real multi-currency org would need server-side aggregation.
                val summary = if (costRows.isEmpty()) {
                    null
                } else {
                    CostSummaryUi(
                        totalAllocated = costRows.sumOf { it.allocatedAmount },
                        totalSpent = costRows.sumOf { it.spentAmount },
                        totalMargin = costRows.sumOf { it.margin },
                        currency = costRows.first().currency,
                        projectCount = costRows.size
                    )
                }
                _uiState.update {
                    it.copy(isLoadingReports = false, costSummary = summary, utilizationRows = utilizationRows)
                }
            }.onFailure { throwable ->
                val forbidden = (throwable as? ApiException)?.statusCode == 403
                _uiState.update {
                    it.copy(
                        isLoadingReports = false,
                        reportsForbidden = forbidden,
                        reportsErrorMessage = if (forbidden) {
                            "Cost summary & utilization need Admin/Manager access (ReportController is role-gated)."
                        } else {
                            throwable.message ?: "Could not load report widgets"
                        }
                    )
                }
            }
        }
    }
}

data class DashboardUiState(
    val isLoadingReports: Boolean = false,
    val reportsForbidden: Boolean = false,
    val reportsErrorMessage: String? = null,
    val costSummary: CostSummaryUi? = null,
    val utilizationRows: List<UtilizationRowResponse> = emptyList(),
    val upcomingAssignments: List<AssignmentLocalEntity> = emptyList(),
    val unreadAlertCount: Int = 0,
    val recentAlerts: List<NotificationLocalEntity> = emptyList()
)

data class CostSummaryUi(
    val totalAllocated: Double,
    val totalSpent: Double,
    val totalMargin: Double,
    val currency: String,
    val projectCount: Int
)
