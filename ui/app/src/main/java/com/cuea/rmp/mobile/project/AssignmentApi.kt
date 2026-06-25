package com.cuea.rmp.mobile.project

import com.cuea.rmp.mobile.core.network.ApiResponse
import com.cuea.rmp.mobile.project.dto.AssignmentResponse
import com.cuea.rmp.mobile.project.dto.UpdateAssignmentRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface AssignmentApi {

    @GET("api/v1/assignments/{id}")
    suspend fun getAssignment(@Path("id") id: String): ApiResponse<AssignmentResponse>

    @PUT("api/v1/assignments/{id}")
    suspend fun updateAssignment(
        @Path("id") id: String,
        @Body request: UpdateAssignmentRequest
    ): ApiResponse<AssignmentResponse>
}

