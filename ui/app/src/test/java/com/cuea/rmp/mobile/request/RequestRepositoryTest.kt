package com.cuea.rmp.mobile.request

import com.cuea.rmp.mobile.core.db.PendingMutationDao
import com.cuea.rmp.mobile.core.db.PendingMutationEntity
import com.cuea.rmp.mobile.core.db.PendingMutationStatus
import com.cuea.rmp.mobile.core.network.ApiResponse
import com.cuea.rmp.mobile.request.dto.CreateRequestRequest
import com.cuea.rmp.mobile.request.dto.RejectRequestRequest
import com.cuea.rmp.mobile.request.dto.RequestResponse
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeRequestDao : RequestDao {
    val rows = MutableStateFlow<List<RequestLocalEntity>>(emptyList())

    override fun observeAll(): Flow<List<RequestLocalEntity>> = rows

    override suspend fun upsertAll(requests: List<RequestLocalEntity>) {
        val byId = rows.value.associateBy { it.id }.toMutableMap()
        requests.forEach { byId[it.id] = it }
        rows.value = byId.values.toList()
    }

    override suspend fun clearAll() {
        rows.value = emptyList()
    }
}

private class FakePendingMutationDaoForRequest : PendingMutationDao {
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

private class FakeRequestApi(
    private val failCreateWith: Throwable? = null
) : RequestApi {
    override suspend fun createRequest(request: CreateRequestRequest): ApiResponse<RequestResponse> {
        failCreateWith?.let { throw it }
        error("not stubbed for success in this test")
    }

    override suspend fun listRequests(status: String?): ApiResponse<List<RequestResponse>> = ApiResponse(success = true, data = emptyList())
    override suspend fun approveRequest(id: String): ApiResponse<RequestResponse> = error("not used")
    override suspend fun rejectRequest(id: String, request: RejectRequestRequest): ApiResponse<RequestResponse> = error("not used")
}

@OptIn(ExperimentalSerializationApi::class)
class RequestRepositoryTest {

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false; isLenient = true }

    @Test
    fun `createRequestOffline queues the create locally while offline rather than losing it`() = runTest {
        val dao = FakeRequestDao()
        val pendingMutationDao = FakePendingMutationDaoForRequest()
        val api = FakeRequestApi(failCreateWith = IOException("offline"))
        val repository = RequestRepository(api, dao, pendingMutationDao, json)

        val localId = repository.createRequestOffline(
            resourceId = "r1",
            projectId = "p1",
            title = "Need extra hands",
            startDate = "2026-08-01",
            endDate = "2026-08-15",
            allocationPct = 50,
            requesterId = "u1"
        )

        // Visible immediately as an optimistic local row, even though the network call failed.
        val cached = dao.observeAll().first()
        assertEquals(1, cached.size)
        assertEquals(localId, cached.first().id)
        assertEquals("PENDING", cached.first().status)

        // Queued for the worker to retry — not silently dropped.
        val pending = pendingMutationDao.listByStatus(listOf(PendingMutationStatus.PENDING, PendingMutationStatus.FAILED), 10)
        assertEquals(1, pending.size)
        assertEquals("REQUEST", pending.first().entityType)
        assertTrue(pending.first().status == PendingMutationStatus.FAILED || pending.first().status == PendingMutationStatus.PENDING)
    }
}
