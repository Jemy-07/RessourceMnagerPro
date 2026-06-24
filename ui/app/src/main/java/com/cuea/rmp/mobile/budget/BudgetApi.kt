package com.cuea.rmp.mobile.budget

import com.cuea.rmp.mobile.budget.dto.AllocateBudgetRequest
import com.cuea.rmp.mobile.budget.dto.BudgetResponse
import com.cuea.rmp.mobile.core.network.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface BudgetApi {

    @GET("api/v1/projects/{projectId}/budget")
    suspend fun getBudget(@Path("projectId") projectId: String): ApiResponse<BudgetResponse>

    @PUT("api/v1/projects/{projectId}/budget")
    suspend fun allocateBudget(
        @Path("projectId") projectId: String,
        @Body request: AllocateBudgetRequest
    ): ApiResponse<BudgetResponse>
}

