package com.cuea.rmp.mobile.project.dto

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class ProjectResponse(
    val id: String,
    val orgId: String,
    val managerId: String,
    val name: String,
    val description: String? = null,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val status: String
)

@Serializable
data class CreateProjectRequest(
    val orgId: String,
    val managerId: String,
    val name: String,
    val description: String? = null,
    val startDate: LocalDate,
    val endDate: LocalDate
)

@Serializable
data class UpdateProjectRequest(
    val name: String,
    val description: String? = null,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val status: String
)

@Serializable
data class AssignmentResponse(
    val id: String,
    val projectId: String,
    val resourceId: String,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val allocationPct: Int,
    val status: String
)

@Serializable
data class AssignResourceRequest(
    val resourceId: String,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val allocationPct: Int
)

@Serializable
data class UpdateAssignmentRequest(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val allocationPct: Int,
    val status: String? = null
)

