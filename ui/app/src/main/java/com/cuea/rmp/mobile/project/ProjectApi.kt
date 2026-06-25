package com.cuea.rmp.mobile.project

import com.cuea.rmp.mobile.core.network.ApiResponse
import com.cuea.rmp.mobile.core.network.PageResult
import com.cuea.rmp.mobile.project.dto.AssignResourceRequest
import com.cuea.rmp.mobile.project.dto.AssignmentResponse
import com.cuea.rmp.mobile.project.dto.CreateProjectRequest
import com.cuea.rmp.mobile.project.dto.ProjectResponse
import com.cuea.rmp.mobile.project.dto.UpdateProjectRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ProjectApi {

    @POST("api/v1/projects")
    suspend fun createProject(@Body request: CreateProjectRequest): ApiResponse<ProjectResponse>

    @PUT("api/v1/projects/{id}")
    suspend fun updateProject(
        @Path("id") id: String,
        @Body request: UpdateProjectRequest
    ): ApiResponse<ProjectResponse>

    @DELETE("api/v1/projects/{id}")
    suspend fun deleteProject(@Path("id") id: String): ApiResponse<Unit>

    @GET("api/v1/projects/{id}")
    suspend fun getProject(@Path("id") id: String): ApiResponse<ProjectResponse>

    @GET("api/v1/projects")
    suspend fun listProjects(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<PageResult<ProjectResponse>>

    @POST("api/v1/projects/{id}/assignments")
    suspend fun assignResource(
        @Path("id") projectId: String,
        @Body request: AssignResourceRequest
    ): ApiResponse<AssignmentResponse>

    @GET("api/v1/projects/{id}/assignments")
    suspend fun getAssignmentsByProject(@Path("id") projectId: String): ApiResponse<List<AssignmentResponse>>
}

