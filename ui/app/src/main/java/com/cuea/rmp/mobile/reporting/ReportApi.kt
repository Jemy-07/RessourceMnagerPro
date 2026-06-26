package com.cuea.rmp.mobile.reporting

import com.cuea.rmp.mobile.core.network.ApiResponse
import com.cuea.rmp.mobile.reporting.dto.CostRowResponse
import com.cuea.rmp.mobile.reporting.dto.SkillsGapRowResponse
import com.cuea.rmp.mobile.reporting.dto.UtilizationRowResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming

interface ReportApi {

    @GET("api/v1/reports/cost")
    suspend fun getCostReport(): ApiResponse<List<CostRowResponse>>

    @GET("api/v1/reports/skills-gap")
    suspend fun getSkillsGapReport(): ApiResponse<List<SkillsGapRowResponse>>

    @GET("api/v1/reports/utilization")
    suspend fun getUtilizationReport(): ApiResponse<List<UtilizationRowResponse>>

    // Unlike the other report endpoints, /export returns a raw PDF byte stream
    // (ResponseEntity<byte[]>), not the ApiResponse<T> JSON envelope — so this is
    // plain Response<ResponseBody> rather than going through safeApiCall.
    @Streaming
    @GET("api/v1/reports/export")
    suspend fun exportReport(
        @Query("type") type: String,
        @Query("format") format: String = "pdf"
    ): Response<ResponseBody>
}
