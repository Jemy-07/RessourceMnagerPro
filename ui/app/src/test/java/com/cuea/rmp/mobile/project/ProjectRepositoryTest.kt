package com.cuea.rmp.mobile.project

import com.cuea.rmp.mobile.core.db.PendingMutationDao
import com.cuea.rmp.mobile.core.db.PendingMutationEntity
import com.cuea.rmp.mobile.core.db.PendingMutationStatus
import com.cuea.rmp.mobile.core.network.ApiResponse
import com.cuea.rmp.mobile.core.network.PageResult
import com.cuea.rmp.mobile.project.dto.AssignResourceRequest
import com.cuea.rmp.mobile.project.dto.AssignmentResponse
import com.cuea.rmp.mobile.project.dto.CreateProjectRequest
import com.cuea.rmp.mobile.project.dto.ProjectResponse
import com.cuea.rmp.mobile.project.dto.UpdateProjectRequest
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

private class FakeProjectDaoForEdit : ProjectDao {
    val rows = MutableStateFlow<List<ProjectLocalEntity>>(emptyList())

    override fun observeAll(): Flow<List<ProjectLocalEntity>> = rows
    override fun observeById(id: String): Flow<ProjectLocalEntity?> = rows.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun upsertAll(projects: List<ProjectLocalEntity>) {
        val byId = rows.value.associateBy { it.id }.toMutableMap()
        projects.forEach { byId[it.id] = it }
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
        description: String?,
        startDate: String,
        endDate: String,
        status: String
    ) {
        rows.value = rows.value.map {
            if (it.id == id) {
                it.copy(name = name, description = description, startDate = startDate, endDate = endDate, status = status, pendingEdit = true)
            } else it
        }
    }

    override suspend fun clearAll() {
        rows.value = emptyList()
    }
}

private class FakePendingMutationDaoForProject : PendingMutationDao {
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

private class FakeProjectApiForEdit : ProjectApi {
    override suspend fun createProject(request: CreateProjectRequest): ApiResponse<ProjectResponse> = error("not used")
    override suspend fun updateProject(id: String, request: UpdateProjectRequest): ApiResponse<ProjectResponse> = error("not used")
    override suspend fun deleteProject(id: String): ApiResponse<Unit> = error("not used")
    override suspend fun getProject(id: String): ApiResponse<ProjectResponse> = error("not used in these tests")
    override suspend fun listProjects(page: Int, size: Int): ApiResponse<PageResult<ProjectResponse>> = error("not used")
    override suspend fun assignResource(projectId: String, request: AssignResourceRequest): ApiResponse<AssignmentResponse> = error("not used")
    override suspend fun getAssignmentsByProject(projectId: String): ApiResponse<List<AssignmentResponse>> = error("not used")
}

@OptIn(ExperimentalSerializationApi::class)
class ProjectRepositoryTest {

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false; isLenient = true }

    @Test
    fun `editProjectOffline applies the edit locally and queues a sync-push mutation`() = runTest {
        val dao = FakeProjectDaoForEdit()
        dao.rows.value = listOf(
            ProjectLocalEntity(
                id = "p1", orgId = "org1", managerId = "m1", name = "Apollo",
                description = "old", startDate = "2026-01-01", endDate = "2026-06-01",
                status = "PLANNED", syncVersion = 2, serverUpdatedAt = "2026-01-01T00:00:00Z"
            )
        )
        val pendingMutationDao = FakePendingMutationDaoForProject()
        val repository = ProjectRepository(FakeProjectApiForEdit(), dao, pendingMutationDao, json)

        repository.editProjectOffline(
            id = "p1",
            name = "Apollo Platform",
            description = "new description",
            startDate = "2026-02-01",
            endDate = "2026-07-01",
            status = "ACTIVE"
        )

        val cached = dao.observeById("p1").first()
        assertEquals("Apollo Platform", cached?.name)
        assertEquals("ACTIVE", cached?.status)
        assertTrue(cached!!.pendingEdit)

        assertEquals(1, pendingMutationDao.rows.size)
        val mutation = pendingMutationDao.rows.first()
        assertEquals("PROJECT", mutation.entityType)
        assertEquals(PendingMutationStatus.PENDING, mutation.status)

        val entry = json.decodeFromString<SyncEntryRequest>(mutation.bodyJson)
        assertEquals("p1", entry.id)
        assertEquals(2L, entry.clientVersion)
        assertEquals("ACTIVE", entry.payload["status"]?.jsonPrimitive?.content)
    }
}
