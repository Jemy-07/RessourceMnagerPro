package com.cuea.rmp.mobile.budget

import com.cuea.rmp.mobile.budget.dto.AllocateBudgetRequest
import com.cuea.rmp.mobile.budget.dto.BudgetResponse
import com.cuea.rmp.mobile.core.network.ApiResponse
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test

private class FakeBudgetDao : BudgetDao {
    val rows = MutableStateFlow<List<BudgetLocalEntity>>(emptyList())

    override fun observeAll(): Flow<List<BudgetLocalEntity>> = rows
    override fun observeByProject(projectId: String): Flow<BudgetLocalEntity?> =
        rows.map { list -> list.firstOrNull { it.projectId == projectId } }

    override suspend fun upsertAll(budgets: List<BudgetLocalEntity>) {
        val byId = rows.value.associateBy { it.id }.toMutableMap()
        budgets.forEach { byId[it.id] = it }
        rows.value = byId.values.toList()
    }

    override suspend fun clearAll() {
        rows.value = emptyList()
    }
}

private class FakeBudgetApi(
    private val onGet: (String) -> BudgetResponse = { error("not stubbed") },
    private val onAllocate: (String, AllocateBudgetRequest) -> BudgetResponse = { _, _ -> error("not stubbed") },
    private val failGetWith: Throwable? = null
) : BudgetApi {
    override suspend fun getBudget(projectId: String): ApiResponse<BudgetResponse> {
        failGetWith?.let { throw it }
        return ApiResponse(success = true, data = onGet(projectId))
    }

    override suspend fun allocateBudget(projectId: String, request: AllocateBudgetRequest): ApiResponse<BudgetResponse> =
        ApiResponse(success = true, data = onAllocate(projectId, request))
}

@OptIn(ExperimentalSerializationApi::class)
class BudgetRepositoryTest {

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false; isLenient = true }

    @Test
    fun `refresh caches margin and remaining exactly as returned by the backend, no client recomputation`() = runTest {
        val dao = FakeBudgetDao()
        val response = BudgetResponse(
            id = "b1", projectId = "p1", currency = "USD",
            totalAmount = 10000.0, allocatedAmount = 8000.0, spentAmount = 3000.0,
            margin = 7000.0, remaining = 2000.0
        )
        val api = FakeBudgetApi(onGet = { response })
        val repository = BudgetRepository(api, dao, json)

        repository.refreshBudget("p1")

        val cached = dao.observeByProject("p1").first()
        assertEquals(7000.0, cached?.margin)
        assertEquals(2000.0, cached?.remaining)
    }

    @Test
    fun `refresh failure leaves no budget cached for that project`() = runTest {
        val dao = FakeBudgetDao()
        val api = FakeBudgetApi(failGetWith = IOException("offline"))
        val repository = BudgetRepository(api, dao, json)

        try {
            repository.refreshBudget("p1")
            fail("expected IOException to propagate")
        } catch (expected: IOException) {
            // expected
        }

        assertNull(dao.observeByProject("p1").first())
    }

    @Test
    fun `allocateBudget persists the backend's computed response`() = runTest {
        val dao = FakeBudgetDao()
        val response = BudgetResponse(
            id = "b1", projectId = "p1", currency = "USD",
            totalAmount = 5000.0, allocatedAmount = 5000.0, spentAmount = 0.0,
            margin = 5000.0, remaining = 0.0
        )
        val api = FakeBudgetApi(onAllocate = { _, _ -> response })
        val repository = BudgetRepository(api, dao, json)

        val result = repository.allocateBudget(
            "p1",
            AllocateBudgetRequest(totalAmount = 5000.0, allocatedAmount = 5000.0, currency = "USD")
        )

        assertEquals(5000.0, result.margin, 0.0)
        assertEquals(5000.0, dao.observeByProject("p1").first()?.totalAmount)
    }
}
