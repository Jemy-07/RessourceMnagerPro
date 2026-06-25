package com.cuea.rmp.mobile.timesheet

import com.cuea.rmp.mobile.core.network.ApiResponse
import com.cuea.rmp.mobile.timesheet.dto.LogTimeRequest
import com.cuea.rmp.mobile.timesheet.dto.TimesheetResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TimesheetApi {

    @POST("api/v1/timesheets")
    suspend fun logTime(@Body request: LogTimeRequest): ApiResponse<TimesheetResponse>

    @POST("api/v1/timesheets/{id}/submit")
    suspend fun submitTimesheet(@Path("id") id: String): ApiResponse<TimesheetResponse>

    @POST("api/v1/timesheets/{id}/approve")
    suspend fun approveTimesheet(@Path("id") id: String): ApiResponse<TimesheetResponse>

    @GET("api/v1/timesheets")
    suspend fun listTimesheets(
        @Query("resourceId") resourceId: String,
        @Query("from") from: String,
        @Query("to") to: String
    ): ApiResponse<List<TimesheetResponse>>
}

