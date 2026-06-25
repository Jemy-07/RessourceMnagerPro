package com.cuea.rmp.mobile.budget

import com.cuea.rmp.mobile.budget.dto.AllocateBudgetRequest
import com.cuea.rmp.mobile.budget.dto.BudgetResponse
import com.cuea.rmp.mobile.core.network.safeApiCall
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

// margin/remaining come straight from the backend's Budget.margin()/remaining()
// (real computed methods, not stored fields) — never recompute them on the client.
@Singleton
class BudgetRepository @Inject constructor(
    private val budgetApi: BudgetApi,
    private val budgetDao: BudgetDao,
    private val json: Json
) {

    fun observeBudgetForProject(projectId: String): Flow<BudgetLocalEntity?> =
        budgetDao.observeByProject(projectId)

    suspend fun refreshBudget(projectId: String) {
        val response = safeApiCall(json) { budgetApi.getBudget(projectId) }
        budgetDao.upsertAll(listOf(response.toLocalEntity()))
    }

    suspend fun allocateBudget(projectId: String, request: AllocateBudgetRequest): BudgetResponse {
        val response = safeApiCall(json) { budgetApi.allocateBudget(projectId, request) }
        budgetDao.upsertAll(listOf(response.toLocalEntity()))
        return response
    }
}

private fun BudgetResponse.toLocalEntity() = BudgetLocalEntity(
    id = id,
    projectId = projectId,
    currency = currency,
    totalAmount = totalAmount,
    allocatedAmount = allocatedAmount,
    spentAmount = spentAmount,
    margin = margin,
    remaining = remaining
)
