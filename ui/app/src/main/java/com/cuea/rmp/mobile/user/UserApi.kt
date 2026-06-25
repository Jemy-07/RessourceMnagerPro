package com.cuea.rmp.mobile.user

import com.cuea.rmp.mobile.core.network.ApiResponse
import com.cuea.rmp.mobile.core.network.PageResult
import com.cuea.rmp.mobile.user.dto.CreateUserRequest
import com.cuea.rmp.mobile.user.dto.UpdateUserRequest
import com.cuea.rmp.mobile.user.dto.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApi {

    @POST("api/v1/users")
    suspend fun createUser(@Body request: CreateUserRequest): ApiResponse<UserResponse>

    @GET("api/v1/users/{id}")
    suspend fun getUser(@Path("id") id: String): ApiResponse<UserResponse>

    @GET("api/v1/users")
    suspend fun listUsers(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<PageResult<UserResponse>>

    @PUT("api/v1/users/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body request: UpdateUserRequest
    ): ApiResponse<UserResponse>

    @POST("api/v1/users/{id}/deactivate")
    suspend fun deactivateUser(@Path("id") id: String): ApiResponse<Unit>
}

