package com.cuea.rmp.mobile.budget

import androidx.room.Entity
import androidx.room.PrimaryKey

// Fields mirror BudgetResponse (budget/dto/BudgetDtos.kt), the shape returned by BudgetApi.
// margin/remaining are server-computed (Budget.java margin()/remaining()), cached as-is.
@Entity(tableName = "budgets")
data class BudgetLocalEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val currency: String,
    val totalAmount: Double,
    val allocatedAmount: Double,
    val spentAmount: Double,
    val margin: Double,
    val remaining: Double
)
