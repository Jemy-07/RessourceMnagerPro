package com.cuea.rmp.mobile.sync

import com.cuea.rmp.mobile.core.db.PendingMutationDao
import com.cuea.rmp.mobile.core.db.PendingMutationEntity
import com.cuea.rmp.mobile.core.db.PendingMutationStatus
import com.cuea.rmp.mobile.core.network.ApiException
import com.cuea.rmp.mobile.core.network.ApiResponse
import com.cuea.rmp.mobile.core.network.PageResult
import com.cuea.rmp.mobile.project.ProjectApi
import com.cuea.rmp.mobile.project.ProjectDao
import com.cuea.rmp.mobile.project.ProjectLocalEntity
import com.cuea.rmp.mobile.project.ProjectRepository
import com.cuea.rmp.mobile.project.dto.AssignResourceRequest
import com.cuea.rmp.mobile.project.dto.AssignmentResponse
import com.cuea.rmp.mobile.project.dto.CreateProjectRequest
import com.cuea.rmp.mobile.project.dto.ProjectResponse
import com.cuea.rmp.mobile.project.dto.UpdateProjectRequest
import com.cuea.rmp.mobile.resource.ResourceApi
import com.cuea.rmp.mobile.resource.ResourceDao
import com.cuea.rmp.mobile.resource.ResourceLocalEntity
import com.cuea.rmp.mobile.resource.ResourceRepository
import com.cuea.rmp.mobile.resource.dto.AddSkillRequest
import com.cuea.rmp.mobile.resource.dto.AvailabilityResponse
import com.cuea.rmp.mobile.resource.dto.CreateResourceRequest
import com.cuea.rmp.mobile.resource.dto.ResourceMatchResponse
import com.cuea.rmp.mobile.resource.dto.ResourceResponse
import com.cuea.rmp.mobile.resource.dto.UpdateResourceRequest
import com.cuea.rmp.mobile.sync.dto.ConflictInfoResponse
import com.cuea.rmp.mobile.sync.dto.PullResultResponse
import com.cuea.rmp.mobile.sync.dto.PushResultResponse
import com.cuea.rmp.mobile.sync.dto.SyncEntryRequest
import com.cuea.rmp.mobile.sync.dto.SyncPushRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeResourceDaoForSync : ResourceDao {
    val rows = MutableStateFlow<List<ResourceLocalEntity>>(emptyList())
    override fun observeAll(): Flow<List<ResourceLocalEntity>> = rows
    override fun observeById(id: String): Flow<ResourceLocalEntity?> = rows.map { it.firstOrNull { r -> r.id == id } }
    override suspend fun upsertAll(resources: List<ResourceLocalEntity>) {
        val byId = rows.value.associateBy { it.id }.toMutableMap()
        resources.forEach { byId[it.id] = it }
        rows.value = byId.values.toList()
    }
    override suspend fun applySyncMetadata(id: String, version: Long, updatedAt: String) {
        rows.value = rows.value.map { if (it.id == id) it.copy(syncVersion = version, serverUpdatedAt = updatedAt, pendingEdit = false) else it }
    }
    override suspend fun applyLocalEdit(id: String, name: String, hourlyRateAmount: Double, currency: String, availabilityStatus: String) {
        rows.value = rows.value.map { if (it.id == id) it.copy(name = name, hourlyRateAmount = hourlyRateAmount, currency = currency, availabilityStatus = availabilityStatus, pendingEdit = true) else it }
    }
    override suspend fun clearAll() { rows.value = emptyList() }
}

private class FakeResourceApiForSync(
    private val resource: ResourceResponse
) : ResourceApi {
    override suspend fun createResource(request: CreateResourceRequest): ApiResponse<ResourceResponse> = error("not used")
    override suspend fun updateResource(id: String, request: UpdateResourceRequest): ApiResponse<ResourceResponse> = error("not used")
    override suspend fun deleteResource(id: String): ApiResponse<Unit> = error("not used")
    override suspend fun addSkill(id: String, request: AddSkillRequest): ApiResponse<ResourceResponse> = error("not used")
    override suspend fun getResource(id: String): ApiResponse<ResourceResponse> = ApiResponse(success = true, data = resource)
    override suspend fun listResources(page: Int, size: Int): ApiResponse<PageResult<ResourceResponse>> = error("not used")
    override suspend fun checkAvailability(id: String, from: String, to: String): ApiResponse<AvailabilityResponse> = error("not used")
    override suspend fun matchResources(skillId: String, from: String, to: String): ApiResponse<List<ResourceMatchResponse>> = error("not used")
}

private class FakeProjectDaoForSync : ProjectDao {
    val rows = MutableStateFlow<List<ProjectLocalEntity>>(emptyList())
    override fun observeAll(): Flow<List<ProjectLocalEntity>> = rows
    override fun observeById(id: String): Flow<ProjectLocalEntity?> = rows.map { it.firstOrNull { p -> p.id == id } }
    override suspend fun upsertAll(projects: List<ProjectLocalEntity>) {
        val byId = rows.value.associateBy { it.id }.toMutableMap()
        projects.forEach { byId[it.id] = it }
        rows.value = byId.values.toList()
    }
    override suspend fun applySyncMetadata(id: String, version: Long, updatedAt: String) {
        rows.value = rows.value.map { if (it.id == id) it.copy(syncVersion = version, serverUpdatedAt = updatedAt, pendingEdit = false) else it }
    }
    override suspend fun applyLocalEdit(id: String, name: String, description: String?, startDate: String, endDate: String, status: String) {
        rows.value = rows.value.map { if (it.id == id) it.copy(name = name, description = description, startDate = startDate, endDate = endDate, status = status, pendingEdit = true) else it }
    }
    override suspend fun clearAll() { rows.value = emptyList() }
}

private class FakeProjectApiForSync : ProjectApi {
    override suspend fun createProject(request: CreateProjectRequest): ApiResponse<ProjectResponse> = error("not used")
    override suspend fun updateProject(id: String, request: UpdateProjectRequest): ApiResponse<ProjectResponse> = error("not used")
    override suspend fun deleteProject(id: String): ApiResponse<Unit> = error("not used")
    override suspend fun getProject(id: String): ApiResponse<ProjectResponse> = error("not used in this test")
    override suspend fun listProjects(page: Int, size: Int): ApiResponse<PageResult<ProjectResponse>> = error("not used")
    override suspend fun assignResource(projectId: String, request: AssignResourceRequest): ApiResponse<AssignmentResponse> = error("not used")
    override suspend fun getAssignmentsByProject(projectId: String): ApiResponse<List<AssignmentResponse>> = error("not used")
}

private class FakePendingMutationDaoForSync : PendingMutationDao {
    val state = MutableStateFlow<List<PendingMutationEntity>>(emptyList())

    override suspend fun upsert(entity: PendingMutationEntity) {
        state.update { list -> list.filterNot { it.localId == entity.localId } + entity }
    }
    override suspend fun listByStatus(statuses: List<PendingMutationStatus>, limit: Int): List<PendingMutationEntity> =
        state.value.filter { it.status in statuses }.sortedBy { it.createdAt }.take(limit)
    override suspend fun getById(localId: String): PendingMutationEntity? = state.value.firstOrNull { it.localId == localId }
    override fun observeAll(): Flow<List<PendingMutationEntity>> = state
    override suspend fun updateStatus(localId: String, status: PendingMutationStatus, lastError: String?, permanentFailure: Boolean) {
        state.update { list ->
            list.map { if (it.localId == localId) it.copy(status = status, lastError = lastError, permanentFailure = permanentFailure) else it }
        }
    }
}

private class FakeAuditLogDao : AuditLogDao {
    val rows = MutableStateFlow<List<AuditLogLocalEntity>>(emptyList())
    override fun observeAll(): Flow<List<AuditLogLocalEntity>> = rows
    override suspend fun upsertAll(auditLogs: List<AuditLogLocalEntity>) {
        rows.value = rows.value + auditLogs
    }
    override suspend fun clearAll() { rows.value = emptyList() }
}

private class FakeSyncApi(
    private val pushResult: PushResultResponse? = null,
    private val pullResult: PullResultResponse = PullResultResponse("2026-01-01T00:00:00Z", 0, emptyList()),
    private val failPushWith: Throwable? = null
) : SyncApi {
    var lastPushRequest: SyncPushRequest? = null

    override suspend fun push(request: SyncPushRequest): ApiResponse<PushResultResponse> {
        lastPushRequest = request
        failPushWith?.let { throw it }
        return ApiResponse(success = true, data = pushResult)
    }

    override suspend fun pull(since: String?): ApiResponse<PullResultResponse> = ApiResponse(success = true, data = pullResult)
}

@OptIn(ExperimentalSerializationApi::class)
class SyncRepositoryTest {

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false; isLenient = true }

    private fun buildResourceRepository(api: ResourceApi, dao: ResourceDao) =
        ResourceRepository(api, dao, FakePendingMutationDaoForSync(), json)

    private fun buildProjectRepository() =
        ProjectRepository(FakeProjectApiForSync(), FakeProjectDaoForSync(), FakePendingMutationDaoForSync(), json)

    @Test
    fun `a SERVER_WON conflict is recorded to the audit log and the pending mutation is marked synced`() = runTest {
        val resourceDao = FakeResourceDaoForSync()
        resourceDao.rows.value = listOf(
            ResourceLocalEntity(
                id = "r1", orgId = "org1", userId = null, name = "Alice (edited locally)", type = "HUMAN",
                hourlyRateAmount = 999.0, currency = "USD", availabilityStatus = "BUSY",
                skillsSummary = "", syncVersion = 1, pendingEdit = true
            )
        )
        val serverTruth = ResourceResponse(
            id = "r1", orgId = "org1", userId = null, name = "Alice (server)", type = "HUMAN",
            hourlyRateAmount = 60.0, currency = "USD", availabilityStatus = "AVAILABLE"
        )
        val resourceApi = FakeResourceApiForSync(serverTruth)
        val resourceRepository = buildResourceRepository(resourceApi, resourceDao)

        val pendingMutationDao = FakePendingMutationDaoForSync()
        val entry = SyncEntryRequest(
            entityType = "RESOURCE",
            id = "r1",
            payload = emptyMap(),
            clientUpdatedAt = "2026-01-01T00:00:00Z",
            clientVersion = 1
        )
        pendingMutationDao.upsert(
            PendingMutationEntity(
                localId = "RESOURCE:r1",
                entityType = "RESOURCE",
                httpMethod = "POST",
                path = "api/v1/sync/push",
                bodyJson = json.encodeToString(entry),
                createdAt = 0L,
                status = PendingMutationStatus.PENDING
            )
        )

        val auditLogDao = FakeAuditLogDao()
        val conflict = ConflictInfoResponse(
            entityType = "RESOURCE",
            id = "r1",
            resolution = "SERVER_WON",
            message = "Concurrent edit: server is newer — client change rejected"
        )
        val syncApi = FakeSyncApi(pushResult = PushResultResponse(appliedCount = 0, conflictCount = 1, conflicts = listOf(conflict)))

        val syncRepository = SyncRepository(
            syncApi = syncApi,
            pendingMutationDao = pendingMutationDao,
            auditLogDao = auditLogDao,
            resourceRepository = resourceRepository,
            projectRepository = buildProjectRepository(),
            json = json
        )

        syncRepository.pushPendingMutations()

        val conflicts = syncRepository.observeConflicts().first()
        assertEquals(1, conflicts.size)
        assertEquals("SERVER_WON", conflicts.first().action)
        assertTrue(conflicts.first().conflict)
        assertEquals("RESOURCE", conflicts.first().entityType)
        assertEquals("r1", conflicts.first().entityId)

        val mutation = pendingMutationDao.getById("RESOURCE:r1")
        assertEquals(PendingMutationStatus.SYNCED, mutation?.status)

        // The local cache reflects the server's truth, not the rejected local edit.
        val cached = resourceDao.observeById("r1").first()
        assertEquals("Alice (server)", cached?.name)
        assertEquals(60.0, cached?.hourlyRateAmount)
    }

    @Test
    fun `a clean push with no conflicts does not write anything to the audit log`() = runTest {
        val resourceDao = FakeResourceDaoForSync()
        resourceDao.rows.value = listOf(
            ResourceLocalEntity(
                id = "r2", orgId = "org1", userId = null, name = "Bob", type = "HUMAN",
                hourlyRateAmount = 80.0, currency = "USD", availabilityStatus = "AVAILABLE",
                skillsSummary = "", syncVersion = 0
            )
        )
        val resourceApi = FakeResourceApiForSync(
            ResourceResponse(id = "r2", orgId = "org1", userId = null, name = "Bob", type = "HUMAN", hourlyRateAmount = 80.0, currency = "USD", availabilityStatus = "AVAILABLE")
        )
        val resourceRepository = buildResourceRepository(resourceApi, resourceDao)

        val pendingMutationDao = FakePendingMutationDaoForSync()
        val entry = SyncEntryRequest("RESOURCE", "r2", emptyMap(), "2026-01-01T00:00:00Z", 0)
        pendingMutationDao.upsert(
            PendingMutationEntity("RESOURCE:r2", "RESOURCE", "POST", "api/v1/sync/push", json.encodeToString(entry), 0L, PendingMutationStatus.PENDING)
        )

        val auditLogDao = FakeAuditLogDao()
        val syncApi = FakeSyncApi(pushResult = PushResultResponse(appliedCount = 1, conflictCount = 0, conflicts = emptyList()))

        val syncRepository = SyncRepository(syncApi, pendingMutationDao, auditLogDao, resourceRepository, buildProjectRepository(), json)

        syncRepository.pushPendingMutations()

        assertTrue(syncRepository.observeConflicts().first().isEmpty())
        assertEquals(PendingMutationStatus.SYNCED, pendingMutationDao.getById("RESOURCE:r2")?.status)
    }

    @Test
    fun `a permanent push failure is surfaced as UI-facing state instead of being silently discarded`() = runTest {
        val resourceDao = FakeResourceDaoForSync()
        resourceDao.rows.value = listOf(
            ResourceLocalEntity(
                id = "r3", orgId = "org1", userId = null, name = "Carol", type = "HUMAN",
                hourlyRateAmount = 90.0, currency = "USD", availabilityStatus = "AVAILABLE",
                skillsSummary = "", syncVersion = 0
            )
        )
        val resourceApi = FakeResourceApiForSync(
            ResourceResponse(id = "r3", orgId = "org1", userId = null, name = "Carol", type = "HUMAN", hourlyRateAmount = 90.0, currency = "USD", availabilityStatus = "AVAILABLE")
        )
        val resourceRepository = buildResourceRepository(resourceApi, resourceDao)

        val pendingMutationDao = FakePendingMutationDaoForSync()
        val entry = SyncEntryRequest("RESOURCE", "r3", emptyMap(), "2026-01-01T00:00:00Z", 0)
        pendingMutationDao.upsert(
            PendingMutationEntity("RESOURCE:r3", "RESOURCE", "POST", "api/v1/sync/push", json.encodeToString(entry), 0L, PendingMutationStatus.PENDING)
        )

        // Simulates the real 422 observed live in the Cleanup Half-Sprint: a bad enum
        // value rejected by the backend's deserialization, surfaced as a 4xx ApiException.
        val validationFailure = ApiException(code = "SYNC_APPLY_FAILED", message = "Cannot deserialize value of type AvailabilityStatus from String \"BUSY\"", statusCode = 422)
        val syncApi = FakeSyncApi(failPushWith = validationFailure)
        val syncRepository = SyncRepository(syncApi, pendingMutationDao, FakeAuditLogDao(), resourceRepository, buildProjectRepository(), json)

        // Previously this exception propagated out and was swallowed by a bare
        // runCatching {} at every call site — nothing else observed it.
        runCatching { syncRepository.pushPendingMutations() }

        val failures = syncRepository.observeSyncFailures().first()
        assertEquals(1, failures.size)
        assertEquals("RESOURCE:r3", failures.first().localId)
        assertTrue(failures.first().isPermanent)
        assertTrue(failures.first().message.contains("AvailabilityStatus"))

        assertEquals(PendingMutationStatus.FAILED, pendingMutationDao.getById("RESOURCE:r3")?.status)
        assertTrue(pendingMutationDao.getById("RESOURCE:r3")?.permanentFailure == true)
    }

    @Test
    fun `pushMutation targets one specific mutation rather than whatever is oldest in the queue`() = runTest {
        val resourceDao = FakeResourceDaoForSync()
        resourceDao.rows.value = listOf(
            ResourceLocalEntity(
                id = "stuck", orgId = "org1", userId = null, name = "Stuck", type = "HUMAN",
                hourlyRateAmount = 1.0, currency = "USD", availabilityStatus = "AVAILABLE",
                skillsSummary = "", syncVersion = 0
            ),
            ResourceLocalEntity(
                id = "fresh", orgId = "org1", userId = null, name = "Fresh", type = "HUMAN",
                hourlyRateAmount = 2.0, currency = "USD", availabilityStatus = "AVAILABLE",
                skillsSummary = "", syncVersion = 0
            )
        )
        val resourceApi = FakeResourceApiForSync(
            ResourceResponse(id = "fresh", orgId = "org1", userId = null, name = "Fresh", type = "HUMAN", hourlyRateAmount = 2.0, currency = "USD", availabilityStatus = "AVAILABLE")
        )
        val resourceRepository = buildResourceRepository(resourceApi, resourceDao)

        val pendingMutationDao = FakePendingMutationDaoForSync()
        val stuckEntry = SyncEntryRequest("RESOURCE", "stuck", emptyMap(), "2026-01-01T00:00:00Z", 0)
        pendingMutationDao.upsert(
            PendingMutationEntity(
                "RESOURCE:stuck", "RESOURCE", "POST", "api/v1/sync/push",
                json.encodeToString(stuckEntry), createdAt = 0L,
                status = PendingMutationStatus.FAILED, lastError = "stuck forever"
            )
        )
        val freshEntry = SyncEntryRequest("RESOURCE", "fresh", emptyMap(), "2026-06-01T00:00:00Z", 0)
        pendingMutationDao.upsert(
            PendingMutationEntity(
                "RESOURCE:fresh", "RESOURCE", "POST", "api/v1/sync/push",
                json.encodeToString(freshEntry), createdAt = 1_000_000L,
                status = PendingMutationStatus.PENDING
            )
        )

        // A push that always succeeds — if pushMutation accidentally grabbed the oldest
        // (stuck) entry instead of the targeted one, this fake would still report success,
        // so the real assertion is in which row ends up SYNCED afterward.
        val syncApi = FakeSyncApi(pushResult = PushResultResponse(appliedCount = 1, conflictCount = 0, conflicts = emptyList()))
        val syncRepository = SyncRepository(syncApi, pendingMutationDao, FakeAuditLogDao(), resourceRepository, buildProjectRepository(), json)

        syncRepository.pushMutation("RESOURCE:fresh")

        assertEquals(PendingMutationStatus.SYNCED, pendingMutationDao.getById("RESOURCE:fresh")?.status)
        // Untouched — proves the targeted push never looked at the older stuck row.
        assertEquals(PendingMutationStatus.FAILED, pendingMutationDao.getById("RESOURCE:stuck")?.status)
        assertEquals("stuck forever", pendingMutationDao.getById("RESOURCE:stuck")?.lastError)
        assertEquals(1, (syncApi.lastPushRequest?.changes?.size ?: 0))
    }
}
