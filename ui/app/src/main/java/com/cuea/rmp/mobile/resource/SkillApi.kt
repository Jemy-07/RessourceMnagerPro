package com.cuea.rmp.mobile.resource

import com.cuea.rmp.mobile.core.network.ApiResponse
import com.cuea.rmp.mobile.resource.dto.CreateSkillRequest
import com.cuea.rmp.mobile.resource.dto.SkillResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SkillApi {

    @POST("api/v1/skills")
    suspend fun createSkill(@Body request: CreateSkillRequest): ApiResponse<SkillResponse>

    @GET("api/v1/skills")
    suspend fun listSkills(): ApiResponse<List<SkillResponse>>
}

