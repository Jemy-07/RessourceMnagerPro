package com.cuea.rmp.mobile.sync

import com.cuea.rmp.mobile.core.network.ApiResponse
import com.cuea.rmp.mobile.sync.dto.PullResultResponse
import com.cuea.rmp.mobile.sync.dto.PushResultResponse
import com.cuea.rmp.mobile.sync.dto.SyncPushRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SyncApi {

    @POST("api/v1/sync/push")
    suspend fun push(@Body request: SyncPushRequest): ApiResponse<PushResultResponse>

    @GET("api/v1/sync/pull")
    suspend fun pull(@Query("since") since: String? = null): ApiResponse<PullResultResponse>
}
