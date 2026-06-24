package com.cuea.rmp.mobile.resource.dto

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class ResourceSkillResponse(
    val skillId: String,
    val proficiency: Int
)

@Serializable
data class ResourceResponse(
    val id: String,
    val orgId: String,
    val userId: String? = null,
    val name: String,
    val type: String,
    val hourlyRateAmount: Double,
    val currency: String,
    val availabilityStatus: String,
    val skills: List<ResourceSkillResponse> = emptyList()
)

@Serializable
data class CreateResourceRequest(
    val orgId: String,
    val userId: String? = null,
    val name: String,
    val type: String,
    val hourlyRateAmount: Double,
    val currency: String
)

@Serializable
data class UpdateResourceRequest(
    val name: String,
    val hourlyRateAmount: Double,
    val currency: String,
    val availabilityStatus: String
)

@Serializable
data class AddSkillRequest(
    val skillId: String,
    val proficiency: Int
)

@Serializable
data class AvailabilityResponse(
    val resourceId: String,
    val from: LocalDate,
    val to: LocalDate,
    val available: Boolean,
    val reason: String
)

@Serializable
data class ResourceMatchResponse(
    val resourceId: String,
    val name: String,
    val type: String,
    val proficiency: Int,
    val hourlyRateAmount: Double,
    val currency: String
)

@Serializable
data class SkillResponse(
    val id: String,
    val orgId: String,
    val name: String
)

@Serializable
data class CreateSkillRequest(
    val orgId: String,
    val name: String
)

