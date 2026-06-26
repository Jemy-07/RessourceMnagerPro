package com.cuea.rmp.mobile.project

import com.cuea.rmp.mobile.core.network.ApiResponse
import com.cuea.rmp.mobile.project.dto.AssignResourceRequest
import com.cuea.rmp.mobile.project.dto.AssignmentResponse
import com.cuea.rmp.mobile.project.dto.CreateProjectRequest
import com.cuea.rmp.mobile.project.dto.ProjectResponse
import com.cuea.rmp.mobile.project.dto.UpdateAssignmentRequest
import com.cuea.rmp.mobile.project.dto.UpdateProjectRequest
import com.cuea.rmp.mobile.core.network.PageResult
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

private class FakeAssignmentDao : AssignmentDao {
    val rows = MutableStateFlow<List<AssignmentLocalEntity>>(emptyList())

    override fun observeAll(): Flow<List<AssignmentLocalEntity>> = rows
    override fun observeByProject(projectId: String): Flow<List<AssignmentLocalEntity>> = rows
    override fun observeByResource(resourceId: String): Flow<List<AssignmentLocalEntity>> = rows
    override fun observeUpcoming(today: String, limit: Int): Flow<List<AssignmentLocalEntity>> = rows

    override suspend fun upsertAll(assignments: List<AssignmentLocalEntity>) {
        val byId = rows.value.associateBy { it.id }.toMutableMap()
        assignments.forEach { byId[it.id] = it }
        rows.value = byId.values.toList()
    }

    override suspend fun clearByProject(projectId: String) {
        rows.value = rows.value.filterNot { it.projectId == projectId }
    }

    override suspend fun clearAll() {
        rows.value = emptyList()
    }
}

private class FakeAssignmentApi : AssignmentApi {
    override suspend fun getAssignment(id: String): ApiResponse<AssignmentResponse> = error("not used in these tests")

    override suspend fun updateAssignment(
        id: String,
        request: UpdateAssignmentRequest
    ): ApiResponse<AssignmentResponse> = error("not used in these tests")
}

private class FakeProjectApi(
    private val onAssign: (String, AssignResourceRequest) -> AssignmentResponse = { _, _ -> error("not stubbed") },
    private val onListByProject: (String) -> List<AssignmentResponse> = { error("not stubbed") },
    private val failListWith: Throwable? = null
) : ProjectApi {
    override suspend fun createProject(request: CreateProjectRequest): ApiResponse<ProjectResponse> = error("not used")
    override suspend fun updateProject(id: String, request: UpdateProjectRequest): ApiResponse<ProjectResponse> = error("not used")
    override suspend fun deleteProject(id: String): ApiResponse<Unit> = error("not used")
    override suspend fun getProject(id: String): ApiResponse<ProjectResponse> = error("not used")
    override suspend fun listProjects(page: Int, size: Int): ApiResponse<PageResult<ProjectResponse>> = error("not used")

    override suspend fun assignResource(projectId: String, request: AssignResourceRequest): ApiResponse<AssignmentResponse> =
        ApiResponse(success = true, data = onAssign(projectId, request))

    override suspend fun getAssignmentsByProject(projectId: String): ApiResponse<List<AssignmentResponse>> {
        failListWith?.let { throw it }
        return ApiResponse(success = true, data = onListByProject(projectId))
    }
}

@OptIn(ExperimentalSerializationApi::class)
class AssignmentRepositoryTest {

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false; isLenient = true }

    @Test
    fun `refresh caches a project's assignments without touching other projects' cached rows`() = runTest {
        val dao = FakeAssignmentDao()
        dao.rows.value = listOf(
            AssignmentLocalEntity("other-1", "other-project", "r1", "t", "2026-01-01", "2026-01-02", 50, "ACTIVE")
        )
        val response = AssignmentResponse(
            id = "a1", projectId = "p1", resourceId = "r1", title = "Build",
            startDate = LocalDate.parse("2026-01-01"), endDate = LocalDate.parse("2026-02-01"),
            allocationPct = 80, status = "ACTIVE"
        )
        val projectApi = FakeProjectApi(onListByProject = { listOf(response) })
        val repository = AssignmentRepository(FakeAssignmentApi(), projectApi, dao, json)

        repository.refreshAssignmentsForProject("p1")

        val cached = dao.rows.first()
        assertEquals(2, cached.size)
        assertTrue(cached.any { it.id == "a1" && it.projectId == "p1" })
        assertTrue(cached.any { it.id == "other-1" && it.projectId == "other-project" })
    }

    @Test
    fun `refresh failure leaves the local cache untouched`() = runTest {
        val dao = FakeAssignmentDao()
        val existing = AssignmentLocalEntity("a1", "p1", "r1", "t", "2026-01-01", "2026-01-02", 50, "ACTIVE")
        dao.rows.value = listOf(existing)
        val projectApi = FakeProjectApi(failListWith = IOException("offline"))
        val repository = AssignmentRepository(FakeAssignmentApi(), projectApi, dao, json)

        try {
            repository.refreshAssignmentsForProject("p1")
            fail("expected IOException to propagate")
        } catch (expected: IOException) {
            // expected — a failed refresh must not corrupt the cache
        }

        assertEquals(listOf(existing), dao.rows.first())
    }

    @Test
    fun `createAssignment persists the backend response into the local cache`() = runTest {
        val dao = FakeAssignmentDao()
        val response = AssignmentResponse(
            id = "a2", projectId = "p1", resourceId = "r2", title = "Wire up",
            startDate = LocalDate.parse("2026-03-01"), endDate = LocalDate.parse("2026-03-15"),
            allocationPct = 60, status = "ACTIVE"
        )
        val projectApi = FakeProjectApi(onAssign = { _, _ -> response })
        val repository = AssignmentRepository(FakeAssignmentApi(), projectApi, dao, json)

        val result = repository.createAssignment(
            "p1",
            AssignResourceRequest(
                resourceId = "r2", title = "Wire up",
                startDate = LocalDate.parse("2026-03-01"), endDate = LocalDate.parse("2026-03-15"),
                allocationPct = 60
            )
        )

        assertEquals("a2", result.id)
        assertTrue(dao.rows.first().any { it.id == "a2" })
    }
}
