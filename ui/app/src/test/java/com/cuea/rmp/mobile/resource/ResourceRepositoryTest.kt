package com.cuea.rmp.mobile.resource

import com.cuea.rmp.mobile.core.db.PendingMutationDao
import com.cuea.rmp.mobile.core.db.PendingMutationEntity
import com.cuea.rmp.mobile.core.db.PendingMutationStatus
import com.cuea.rmp.mobile.core.network.ApiResponse
import com.cuea.rmp.mobile.core.network.PageResult
import com.cuea.rmp.mobile.resource.dto.AvailabilityResponse
import com.cuea.rmp.mobile.resource.dto.ResourceMatchResponse
import com.cuea.rmp.mobile.resource.dto.ResourceResponse
import com.cuea.rmp.mobile.resource.dto.UpdateResourceRequest
import com.cuea.rmp.mobile.sync.dto.SyncEntryRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeResourceDao : ResourceDao {
    val rows = MutableStateFlow<List<ResourceLocalEntity>>(emptyList())

    override fun observeAll(): Flow<List<ResourceLocalEntity>> = rows
    override fun observeById(id: String): Flow<ResourceLocalEntity?> = rows.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun upsertAll(resources: List<ResourceLocalEntity>) {
        val byId = rows.value.associateBy { it.id }.toMutableMap()
        resources.forEach { byId[it.id] = it }
        rows.value = byId.values.toList()
    }

    override suspend fun applySyncMetadata(id: String, version: Long, updatedAt: String) {
        rows.value = rows.value.map {
            if (it.id == id) it.copy(syncVersion = version, serverUpdatedAt = updatedAt, pendingEdit = false) else it
        }
    }

    override suspend fun applyLocalEdit(
        id: String,
        name: String,
        hourlyRateAmount: Double,
        currency: String,
        availabilityStatus: String
    ) {
        rows.value = rows.value.map {
            if (it.id == id) {
                it.copy(
                    name = name,
                    hourlyRateAmount = hourlyRateAmount,
                    currency = currency,
                    availabilityStatus = availabilityStatus,
                    pendingEdit = true
                )
            } else it
        }
    }

    override suspend fun clearAll() {
        rows.value = emptyList()
    }
}

private class FakePendingMutationDao : PendingMutationDao {
    val rows = mutableListOf<PendingMutationEntity>()

    override suspend fun upsert(entity: PendingMutationEntity) {
        rows.removeAll { it.localId == entity.localId }
        rows.add(entity)
    }

    override suspend fun listByStatus(statuses: List<PendingMutationStatus>, limit: Int): List<PendingMutationEntity> =
        rows.filter { it.status in statuses }.take(limit)

    override suspend fun getById(localId: String): PendingMutationEntity? = rows.firstOrNull { it.localId == localId }

    override suspend fun updateStatus(localId: String, status: PendingMutationStatus, lastError: String?) {
        val index = rows.indexOfFirst { it.localId == localId }
        if (index >= 0) rows[index] = rows[index].copy(status = status, lastError = lastError)
    }
}

private class FakeResourceApi : ResourceApi {
    override suspend fun createResource(request: com.cuea.rmp.mobile.resource.dto.CreateResourceRequest): ApiResponse<ResourceResponse> = error("not used")
    override suspend fun updateResource(id: String, request: UpdateResourceRequest): ApiResponse<ResourceResponse> = error("not used")
    override suspend fun deleteResource(id: String): ApiResponse<Unit> = error("not used")
    override suspend fun addSkill(id: String, request: com.cuea.rmp.mobile.resource.dto.AddSkillRequest): ApiResponse<ResourceResponse> = error("not used")
    override suspend fun getResource(id: String): ApiResponse<ResourceResponse> = error("not used in these tests")
    override suspend fun listResources(page: Int, size: Int): ApiResponse<PageResult<ResourceResponse>> = error("not used")
    override suspend fun checkAvailability(id: String, from: String, to: String): ApiResponse<AvailabilityResponse> = error("not used")
    override suspend fun matchResources(skillId: String, from: String, to: String): ApiResponse<List<ResourceMatchResponse>> = error("not used")
}

@OptIn(ExperimentalSerializationApi::class)
class ResourceRepositoryTest {

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false; isLenient = true }

    @Test
    fun `editResourceOffline applies the edit locally and queues a sync-push mutation`() = runTest {
        val dao = FakeResourceDao()
        dao.rows.value = listOf(
            ResourceLocalEntity(
                id = "r1", orgId = "org1", userId = null, name = "Alice", type = "HUMAN",
                hourlyRateAmount = 50.0, currency = "USD", availabilityStatus = "AVAILABLE",
                skillsSummary = "", syncVersion = 3, serverUpdatedAt = "2026-01-01T00:00:00Z"
            )
        )
        val pendingMutationDao = FakePendingMutationDao()
        val repository = ResourceRepository(FakeResourceApi(), dao, pendingMutationDao, json)

        repository.editResourceOffline(
            id = "r1",
            name = "Alice Updated",
            hourlyRateAmount = 75.0,
            currency = "EUR",
            availabilityStatus = "BUSY"
        )

        val cached = dao.observeById("r1").first()
        assertEquals("Alice Updated", cached?.name)
        assertEquals(75.0, cached?.hourlyRateAmount)
        assertTrue(cached!!.pendingEdit)

        assertEquals(1, pendingMutationDao.rows.size)
        val mutation = pendingMutationDao.rows.first()
        assertEquals("RESOURCE", mutation.entityType)
        assertEquals(PendingMutationStatus.PENDING, mutation.status)
        assertEquals("api/v1/sync/push", mutation.path)

        val entry = json.decodeFromString<SyncEntryRequest>(mutation.bodyJson)
        assertEquals("r1", entry.id)
        assertEquals(3L, entry.clientVersion)
        assertEquals("EUR", entry.payload["hourlyRateCurrency"]?.jsonPrimitive?.content)
    }
}
