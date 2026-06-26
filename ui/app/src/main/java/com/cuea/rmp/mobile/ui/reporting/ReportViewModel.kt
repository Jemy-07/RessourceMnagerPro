package com.cuea.rmp.mobile.ui.reporting

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuea.rmp.mobile.core.network.ApiException
import com.cuea.rmp.mobile.reporting.ReportExportType
import com.cuea.rmp.mobile.reporting.ReportPdfStorage
import com.cuea.rmp.mobile.reporting.ReportRepository
import com.cuea.rmp.mobile.reporting.dto.CostRowResponse
import com.cuea.rmp.mobile.reporting.dto.SkillsGapRowResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val FORBIDDEN_MESSAGE =
    "Reports are restricted to Admin/Manager roles on the backend (ReportController is " +
        "class-level @PreAuthorize-gated) — ask an admin if you need access."

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val reportPdfStorage: ReportPdfStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isForbidden = false) }
            runCatching {
                coroutineScope {
                    val cost = async { reportRepository.getCostReport() }
                    val skillsGap = async { reportRepository.getSkillsGapReport() }
                    cost.await() to skillsGap.await()
                }
            }.onSuccess { (cost, skillsGap) ->
                _uiState.update {
                    it.copy(isLoading = false, costRows = cost, skillsGapRows = skillsGap)
                }
            }.onFailure { throwable ->
                val forbidden = (throwable as? ApiException)?.statusCode == 403
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isForbidden = forbidden,
                        errorMessage = if (forbidden) FORBIDDEN_MESSAGE else throwable.message ?: "Could not load reports"
                    )
                }
            }
        }
    }

    fun exportPdf(type: ReportExportType) {
        viewModelScope.launch {
            _uiState.update { it.copy(exportingType = type, exportMessage = null) }
            runCatching {
                val bytes = reportRepository.exportReportPdf(type)
                reportPdfStorage.save(type, bytes)
            }.onSuccess { uri ->
                _uiState.update {
                    it.copy(exportingType = null, exportMessage = "Saved PDF to device storage", lastExportedUri = uri)
                }
            }.onFailure { throwable ->
                val forbidden = (throwable as? ApiException)?.statusCode == 403
                _uiState.update {
                    it.copy(
                        exportingType = null,
                        exportMessage = if (forbidden) {
                            "Export blocked: requires Admin/Manager role"
                        } else {
                            throwable.message ?: "Export failed"
                        }
                    )
                }
            }
        }
    }
}

data class ReportsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isForbidden: Boolean = false,
    val costRows: List<CostRowResponse> = emptyList(),
    val skillsGapRows: List<SkillsGapRowResponse> = emptyList(),
    val exportingType: ReportExportType? = null,
    val exportMessage: String? = null,
    val lastExportedUri: Uri? = null
)
