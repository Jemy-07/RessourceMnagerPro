package com.cuea.rmp.mobile.request

import com.cuea.rmp.mobile.core.network.ApiResponse
import com.cuea.rmp.mobile.request.dto.CreateRequestRequest
import com.cuea.rmp.mobile.request.dto.RejectRequestRequest
import com.cuea.rmp.mobile.request.dto.RequestResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RequestApi {

    @POST("api/v1/requests")
    suspend fun createRequest(@Body request: CreateRequestRequest): ApiResponse<RequestResponse>

    @GET("api/v1/requests")
    suspend fun listRequests(@Query("status") status: String? = null): ApiResponse<List<RequestResponse>>

    @POST("api/v1/requests/{id}/approve")
    suspend fun approveRequest(@Path("id") id: String): ApiResponse<RequestResponse>

    @POST("api/v1/requests/{id}/reject")
    suspend fun rejectRequest(
        @Path("id") id: String,
        @Body request: RejectRequestRequest
    ): ApiResponse<RequestResponse>
}

