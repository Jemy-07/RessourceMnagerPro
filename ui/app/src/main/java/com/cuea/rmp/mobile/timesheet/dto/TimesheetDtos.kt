package com.cuea.rmp.mobile.timesheet.dto

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class TimesheetResponse(
    val id: String,
    val resourceId: String,
    val assignmentId: String,
    val workDate: LocalDate,
    val hours: Double,
    val status: String
)

@Serializable
data class LogTimeRequest(
    val id: String,
    val resourceId: String,
    val assignmentId: String,
    val workDate: LocalDate,
    val hours: Double
)

