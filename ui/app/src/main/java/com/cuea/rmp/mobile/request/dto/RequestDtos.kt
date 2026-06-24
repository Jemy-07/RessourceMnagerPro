package com.cuea.rmp.mobile.request.dto

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class RequestResponse(
    val id: String,
    val requesterId: String,
    val approverId: String? = null,
    val resourceId: String,
    val projectId: String,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val allocationPct: Int,
    val status: String,
    val comments: String? = null,
    val decidedAt: Instant? = null
)

@Serializable
data class CreateRequestRequest(
    val resourceId: String,
    val projectId: String,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val allocationPct: Int
)

@Serializable
data class RejectRequestRequest(
    val comments: String
)

