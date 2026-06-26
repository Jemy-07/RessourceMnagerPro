package com.cuea.rmp.mobile.reporting.dto

import kotlinx.serialization.Serializable

@Serializable
data class CostRowResponse(
    val projectId: String,
    val projectName: String,
    val currency: String,
    val totalAmount: Double,
    val allocatedAmount: Double,
    val spentAmount: Double,
    val margin: Double,
    val marginPct: Double
)

@Serializable
data class SkillsGapRowResponse(
    val skillId: String,
    val skillName: String,
    val resourceCount: Long,
    val avgProficiency: Double,
    val gap: Boolean
)

@Serializable
data class UtilizationRowResponse(
    val resourceId: String,
    val resourceName: String,
    val activeAssignments: Long,
    val allocatedPct: Long,
    val level: String
)
