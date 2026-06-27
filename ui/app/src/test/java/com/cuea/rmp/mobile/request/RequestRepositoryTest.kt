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
import kotlinx.datetime.LocalDate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
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

    override suspend fun deleteAllExcept(keepIds: List<String>) {
        rows.value = rows.value.filter { it.id in keepIds }
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

    override fun observeAll(): kotlinx.coroutines.flow.Flow<List<PendingMutationEntity>> =
        kotlinx.coroutines.flow.flowOf(rows)

    override suspend fun updateStatus(localId: String, status: PendingMutationStatus, lastError: String?, permanentFailure: Boolean) {
        val index = rows.indexOfFirst { it.localId == localId }
        if (index >= 0) rows[index] = rows[index].copy(status = status, lastError = lastError, permanentFailure = permanentFailure)
    }
}

private class FakeRequestApi(
    private val failCreateWith: Throwable? = null,
    private val failForResourceIds: Set<String> = emptySet(),
    private val listRequestsResult: List<RequestResponse> = emptyList()
) : RequestApi {
    override suspend fun createRequest(request: CreateRequestRequest): ApiResponse<RequestResponse> {
        if (request.resourceId in failForResourceIds) throw IOException("resource $request.resourceId is permanently broken")
        failCreateWith?.let { throw it }
        return ApiResponse(
            success = true,
            data = RequestResponse(
                id = "server-${request.resourceId}",
                requesterId = "u1",
                resourceId = request.resourceId,
                projectId = request.projectId,
                title = request.title,
                startDate = request.startDate,
                endDate = request.endDate,
                allocationPct = request.allocationPct,
                status = "PENDING"
            )
        )
    }

    override suspend fun listRequests(status: String?): ApiResponse<List<RequestResponse>> = ApiResponse(success = true, data = listRequestsResult)
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

    @Test
    fun `createRequestOffline pushes the new mutation immediately rather than retrying an older stuck one first`() = runTest {
        val dao = FakeRequestDao()
        val pendingMutationDao = FakePendingMutationDaoForRequest()

        // An older mutation that permanently fails (e.g. a real RESOURCE_UNAVAILABLE
        // rejection observed live in Sprint 4) sits at the front of the FIFO queue —
        // createdAt is earlier than anything created in this test.
        val stuckRequest = CreateRequestRequest(
            resourceId = "stuck-resource",
            projectId = "p0",
            title = "Stuck request",
            startDate = LocalDate.parse("2026-01-01"),
            endDate = LocalDate.parse("2026-01-02"),
            allocationPct = 10
        )
        pendingMutationDao.upsert(
            PendingMutationEntity(
                localId = "old-stuck",
                entityType = "REQUEST",
                httpMethod = "POST",
                path = "api/v1/requests",
                bodyJson = json.encodeToString(stuckRequest),
                createdAt = 0L,
                status = PendingMutationStatus.FAILED,
                lastError = "Resource is not available for the requested window"
            )
        )

        val api = FakeRequestApi(failForResourceIds = setOf("stuck-resource"))
        val repository = RequestRepository(api, dao, pendingMutationDao, json)

        val newLocalId = repository.createRequestOffline(
            resourceId = "r1",
            projectId = "p1",
            title = "Brand new request",
            startDate = "2026-08-01",
            endDate = "2026-08-15",
            allocationPct = 50,
            requesterId = "u1"
        )

        // The new mutation synced immediately — it wasn't diverted into retrying the
        // older, unrelated, permanently-failing mutation first (head-of-line blocking).
        val newMutation = pendingMutationDao.getById(newLocalId)
        assertEquals(PendingMutationStatus.SYNCED, newMutation?.status)

        // The old stuck mutation is untouched: still FAILED with its original error,
        // proving the targeted push never even looked at it.
        val oldMutation = pendingMutationDao.getById("old-stuck")
        assertEquals(PendingMutationStatus.FAILED, oldMutation?.status)
        assertEquals("Resource is not available for the requested window", oldMutation?.lastError)
    }

    @Test
    fun `refreshRequests preserves a still-unsynced local placeholder instead of wiping it`() = runTest {
        val dao = FakeRequestDao()
        dao.rows.value = listOf(
            RequestLocalEntity(
                id = "server-1", requesterId = "u1", approverId = null,
                resourceId = "r1", projectId = "p1", title = "Already on server",
                startDate = "2026-01-01", endDate = "2026-01-02", allocationPct = 50,
                status = "PENDING", comments = null, decidedAt = null
            ),
            RequestLocalEntity(
                id = "local-stuck", requesterId = "u1", approverId = null,
                resourceId = "r2", projectId = "p1", title = "Permanently stuck",
                startDate = "2026-02-01", endDate = "2026-02-02", allocationPct = 30,
                status = "PENDING", comments = null, decidedAt = null
            )
        )
        val pendingMutationDao = FakePendingMutationDaoForRequest()
        // The stuck row's create mutation failed permanently (e.g. a real
        // RESOURCE_UNAVAILABLE 409) and has no counterpart in the server's list.
        pendingMutationDao.upsert(
            PendingMutationEntity(
                localId = "local-stuck",
                entityType = "REQUEST",
                httpMethod = "POST",
                path = "api/v1/requests",
                bodyJson = "{}",
                createdAt = 0L,
                status = PendingMutationStatus.FAILED,
                lastError = "Resource is not available for the requested window",
                permanentFailure = true
            )
        )

        val api = FakeRequestApi(
            listRequestsResult = listOf(
                RequestResponse(
                    id = "server-1", requesterId = "u1", resourceId = "r1", projectId = "p1",
                    title = "Already on server", startDate = LocalDate.parse("2026-01-01"),
                    endDate = LocalDate.parse("2026-01-02"), allocationPct = 50, status = "PENDING"
                )
            )
        )
        val repository = RequestRepository(api, dao, pendingMutationDao, json)

        repository.refreshRequests()

        // A plain clear+replace would have wiped "local-stuck" since the server has never
        // heard of it -- it must survive because its mutation is still FAILED, not SYNCED.
        val rows = dao.observeAll().first()
        assertEquals(2, rows.size)
        assertTrue(rows.any { it.id == "server-1" })
        assertTrue(rows.any { it.id == "local-stuck" })
    }

    @Test
    fun `refreshRequests drops the local placeholder once its mutation has synced, leaving no duplicate`() = runTest {
        val dao = FakeRequestDao()
        dao.rows.value = listOf(
            RequestLocalEntity(
                id = "local-id", requesterId = "u1", approverId = null,
                resourceId = "r1", projectId = "p1", title = "Now fixed and retried",
                startDate = "2026-03-01", endDate = "2026-03-02", allocationPct = 20,
                status = "PENDING", comments = null, decidedAt = null
            )
        )
        val pendingMutationDao = FakePendingMutationDaoForRequest()
        // The mutation has already SYNCED (the user fixed the underlying issue and
        // retried) -- its local placeholder row is now superseded by the server's row
        // for that same create, under a different (server-assigned) id.
        pendingMutationDao.upsert(
            PendingMutationEntity(
                localId = "local-id",
                entityType = "REQUEST",
                httpMethod = "POST",
                path = "api/v1/requests",
                bodyJson = "{}",
                createdAt = 0L,
                status = PendingMutationStatus.SYNCED
            )
        )

        val api = FakeRequestApi(
            listRequestsResult = listOf(
                RequestResponse(
                    id = "server-id", requesterId = "u1", resourceId = "r1", projectId = "p1",
                    title = "Now fixed and retried", startDate = LocalDate.parse("2026-03-01"),
                    endDate = LocalDate.parse("2026-03-02"), allocationPct = 20, status = "PENDING"
                )
            )
        )
        val repository = RequestRepository(api, dao, pendingMutationDao, json)

        repository.refreshRequests()

        val rows = dao.observeAll().first()
        assertEquals(1, rows.size)
        assertEquals("server-id", rows.first().id)
    }
}
