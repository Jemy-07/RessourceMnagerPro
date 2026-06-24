package com.cuea.rmp.mobile.resource

import com.cuea.rmp.mobile.core.network.ApiResponse
import com.cuea.rmp.mobile.core.network.PageResult
import com.cuea.rmp.mobile.resource.dto.AddSkillRequest
import com.cuea.rmp.mobile.resource.dto.AvailabilityResponse
import com.cuea.rmp.mobile.resource.dto.CreateResourceRequest
import com.cuea.rmp.mobile.resource.dto.ResourceMatchResponse
import com.cuea.rmp.mobile.resource.dto.ResourceResponse
import com.cuea.rmp.mobile.resource.dto.UpdateResourceRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ResourceApi {

    @POST("api/v1/resources")
    suspend fun createResource(@Body request: CreateResourceRequest): ApiResponse<ResourceResponse>

    @PUT("api/v1/resources/{id}")
    suspend fun updateResource(
        @Path("id") id: String,
        @Body request: UpdateResourceRequest
    ): ApiResponse<ResourceResponse>

    @DELETE("api/v1/resources/{id}")
    suspend fun deleteResource(@Path("id") id: String): ApiResponse<Unit>

    @POST("api/v1/resources/{id}/skills")
    suspend fun addSkill(
        @Path("id") id: String,
        @Body request: AddSkillRequest
    ): ApiResponse<ResourceResponse>

    @GET("api/v1/resources/{id}")
    suspend fun getResource(@Path("id") id: String): ApiResponse<ResourceResponse>

    @GET("api/v1/resources")
    suspend fun listResources(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<PageResult<ResourceResponse>>

    @GET("api/v1/resources/{id}/availability")
    suspend fun checkAvailability(
        @Path("id") id: String,
        @Query("from") from: String,
        @Query("to") to: String
    ): ApiResponse<AvailabilityResponse>

    @GET("api/v1/resources/match")
    suspend fun matchResources(
        @Query("skillId") skillId: String,
        @Query("from") from: String,
        @Query("to") to: String
    ): ApiResponse<List<ResourceMatchResponse>>
}

