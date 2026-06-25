package com.cuea.rmp.mobile.budget.dto

import kotlinx.serialization.Serializable

@Serializable
data class BudgetResponse(
    val id: String,
    val projectId: String,
    val currency: String,
    val totalAmount: Double,
    val allocatedAmount: Double,
    val spentAmount: Double,
    val margin: Double,
    val remaining: Double
)

@Serializable
data class AllocateBudgetRequest(
    val totalAmount: Double,
    val allocatedAmount: Double,
    val currency: String
)

