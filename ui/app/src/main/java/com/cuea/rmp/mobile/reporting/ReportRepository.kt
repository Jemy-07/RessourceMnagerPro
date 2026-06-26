package com.cuea.rmp.mobile.reporting

import com.cuea.rmp.mobile.core.network.ApiException
import com.cuea.rmp.mobile.core.network.parseApiError
import com.cuea.rmp.mobile.core.network.safeApiCall
import com.cuea.rmp.mobile.reporting.dto.CostRowResponse
import com.cuea.rmp.mobile.reporting.dto.SkillsGapRowResponse
import com.cuea.rmp.mobile.reporting.dto.UtilizationRowResponse
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

enum class ReportExportType(val apiValue: String) {
    COST("cost"),
    SKILLS_GAP("skills-gap"),
    UTILIZATION("utilization")
}

// Reports are small, computed-on-demand views (a handful of rows per org) rather
// than offline-editable data, so there is no Room cache here — every screen open
// re-fetches from the backend, same as the live reports the server itself computes.
@Singleton
class ReportRepository @Inject constructor(
    private val reportApi: ReportApi,
    private val json: Json
) {

    suspend fun getCostReport(): List<CostRowResponse> =
        safeApiCall(json) { reportApi.getCostReport() }

    suspend fun getSkillsGapReport(): List<SkillsGapRowResponse> =
        safeApiCall(json) { reportApi.getSkillsGapReport() }

    suspend fun getUtilizationReport(): List<UtilizationRowResponse> =
        safeApiCall(json) { reportApi.getUtilizationReport() }

    suspend fun exportReportPdf(type: ReportExportType): ByteArray {
        val response = reportApi.exportReport(type.apiValue)
        if (!response.isSuccessful) {
            val parsed = parseApiError(
                json = json,
                raw = response.errorBody()?.string(),
                fallbackMessage = "HTTP ${response.code()}"
            )
            throw ApiException(
                code = parsed.errorCode,
                message = parsed.message,
                statusCode = response.code(),
                fieldErrors = parsed.fieldErrors
            )
        }
        return response.body()?.bytes() ?: throw ApiException(message = "Server returned an empty PDF")
    }
}
