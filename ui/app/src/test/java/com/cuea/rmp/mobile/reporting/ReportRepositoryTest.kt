package com.cuea.rmp.mobile.reporting

import com.cuea.rmp.mobile.core.network.ApiException
import com.cuea.rmp.mobile.core.network.ApiResponse
import com.cuea.rmp.mobile.reporting.dto.CostRowResponse
import com.cuea.rmp.mobile.reporting.dto.SkillsGapRowResponse
import com.cuea.rmp.mobile.reporting.dto.UtilizationRowResponse
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

private class FakeReportApi(
    private val onGetCost: () -> List<CostRowResponse> = { error("not stubbed") },
    private val onGetSkillsGap: () -> List<SkillsGapRowResponse> = { error("not stubbed") },
    private val onGetUtilization: () -> List<UtilizationRowResponse> = { error("not stubbed") },
    private val onExport: () -> Response<ResponseBody> = { error("not stubbed") },
    private val failGetCostWith: Throwable? = null
) : ReportApi {

    override suspend fun getCostReport(): ApiResponse<List<CostRowResponse>> {
        failGetCostWith?.let { throw it }
        return ApiResponse(success = true, data = onGetCost())
    }

    override suspend fun getSkillsGapReport(): ApiResponse<List<SkillsGapRowResponse>> =
        ApiResponse(success = true, data = onGetSkillsGap())

    override suspend fun getUtilizationReport(): ApiResponse<List<UtilizationRowResponse>> =
        ApiResponse(success = true, data = onGetUtilization())

    override suspend fun exportReport(type: String, format: String): Response<ResponseBody> = onExport()
}

private fun forbiddenHttpException(): HttpException {
    val body = """{"success":false,"message":"Access denied","errorCode":"FORBIDDEN"}"""
        .toResponseBody("application/json".toMediaType())
    return HttpException(Response.error<Any>(403, body))
}

@OptIn(ExperimentalSerializationApi::class)
class ReportRepositoryTest {

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false; isLenient = true }

    @Test
    fun `getCostReport returns the rows from the backend`() = runTest {
        val rows = listOf(
            CostRowResponse(
                projectId = "p1", projectName = "Apollo Platform", currency = "USD",
                totalAmount = 1000.0, allocatedAmount = 800.0, spentAmount = 600.0,
                margin = 200.0, marginPct = 25.0
            )
        )
        val api = FakeReportApi(onGetCost = { rows })
        val repository = ReportRepository(api, json)

        val result = repository.getCostReport()

        assertEquals(1, result.size)
        assertEquals("Apollo Platform", result.first().projectName)
    }

    @Test
    fun `getCostReport propagates a 403 as an ApiException with statusCode 403`() = runTest {
        val api = FakeReportApi(failGetCostWith = forbiddenHttpException())
        val repository = ReportRepository(api, json)

        try {
            repository.getCostReport()
            fail("expected an ApiException for the 403 response")
        } catch (expected: ApiException) {
            assertEquals(403, expected.statusCode)
            assertEquals("FORBIDDEN", expected.code)
        }
    }

    @Test
    fun `getSkillsGapReport returns the rows from the backend`() = runTest {
        val rows = listOf(
            SkillsGapRowResponse(skillId = "s1", skillName = "Java", resourceCount = 2, avgProficiency = 3.5, gap = false)
        )
        val api = FakeReportApi(onGetSkillsGap = { rows })
        val repository = ReportRepository(api, json)

        val result = repository.getSkillsGapReport()

        assertEquals(1, result.size)
        assertTrue(result.none { it.gap })
    }

    @Test
    fun `exportReportPdf returns the PDF bytes on a successful response`() = runTest {
        val pdfBytes = byteArrayOf(1, 2, 3)
        val api = FakeReportApi(
            onExport = { Response.success(pdfBytes.toResponseBody("application/pdf".toMediaType())) }
        )
        val repository = ReportRepository(api, json)

        val result = repository.exportReportPdf(ReportExportType.COST)

        assertTrue(pdfBytes.contentEquals(result))
    }

    @Test
    fun `exportReportPdf throws ApiException with statusCode 403 when export is role-gated`() = runTest {
        val api = FakeReportApi(
            onExport = {
                Response.error(
                    403,
                    """{"success":false,"message":"Access denied","errorCode":"FORBIDDEN"}"""
                        .toResponseBody("application/json".toMediaType())
                )
            }
        )
        val repository = ReportRepository(api, json)

        try {
            repository.exportReportPdf(ReportExportType.COST)
            fail("expected an ApiException for the 403 export response")
        } catch (expected: ApiException) {
            assertEquals(403, expected.statusCode)
        }
    }
}
