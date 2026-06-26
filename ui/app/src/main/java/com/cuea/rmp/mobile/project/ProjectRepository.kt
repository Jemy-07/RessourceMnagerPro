package com.cuea.rmp.mobile.project

import com.cuea.rmp.mobile.core.db.PendingMutationDao
import com.cuea.rmp.mobile.core.db.PendingMutationEntity
import com.cuea.rmp.mobile.core.db.PendingMutationStatus
import com.cuea.rmp.mobile.core.network.safeApiCall
import com.cuea.rmp.mobile.project.dto.ProjectResponse
import com.cuea.rmp.mobile.sync.dto.SyncEntryRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepository @Inject constructor(
    private val projectApi: ProjectApi,
    private val projectDao: ProjectDao,
    private val pendingMutationDao: PendingMutationDao,
    private val json: Json
) {

    fun observeProjects(): Flow<List<ProjectLocalEntity>> = projectDao.observeAll()

    fun observeProject(id: String): Flow<ProjectLocalEntity?> = projectDao.observeById(id)

    suspend fun refreshProjects(pageSize: Int = 50) {
        val aggregated = mutableListOf<ProjectLocalEntity>()
        var page = 0
        var totalPages = 1

        while (page < totalPages) {
            val response = safeApiCall(json) {
                projectApi.listProjects(page = page, size = pageSize)
            }

            aggregated += response.content.map { it.toLocalEntity() }

            page += 1
            totalPages = response.totalPages
        }

        projectDao.clearAll()
        projectDao.upsertAll(aggregated)
    }

    /** Targeted single-project refresh for Project Detail — avoids re-pulling the full list. */
    suspend fun refreshProject(id: String) {
        val response = safeApiCall(json) { projectApi.getProject(id) }
        projectDao.upsertAll(listOf(response.toLocalEntity()))
    }

    suspend fun applySyncMetadata(id: String, version: Long, updatedAt: String) {
        projectDao.applySyncMetadata(id, version, updatedAt)
    }

    // Offline edit: same generic-sync-engine pattern as ResourceRepository.editResourceOffline
    // — see that doc comment for why this goes through /sync/push and not the regular PUT.
    suspend fun editProjectOffline(
        id: String,
        name: String,
        description: String?,
        startDate: String,
        endDate: String,
        status: String
    ) {
        val current = projectDao.observeById(id).first()
            ?: error("Project $id is not cached locally — refresh before editing")
        val now = Clock.System.now()

        projectDao.applyLocalEdit(
            id = id,
            name = name,
            description = description,
            startDate = startDate,
            endDate = endDate,
            status = status
        )

        val entry = SyncEntryRequest(
            entityType = "PROJECT",
            id = id,
            payload = buildMap {
                put("name", JsonPrimitive(name))
                put("description", description?.let { JsonPrimitive(it) } ?: JsonNull)
                put("startDate", JsonPrimitive(startDate))
                put("endDate", JsonPrimitive(endDate))
                put("status", JsonPrimitive(status))
            },
            clientUpdatedAt = now.toString(),
            clientVersion = current.syncVersion
        )

        pendingMutationDao.upsert(
            PendingMutationEntity(
                localId = "PROJECT:$id",
                entityType = "PROJECT",
                httpMethod = "POST",
                path = "api/v1/sync/push",
                bodyJson = json.encodeToString(entry),
                createdAt = now.toEpochMilliseconds(),
                status = PendingMutationStatus.PENDING
            )
        )
    }
}

private fun ProjectResponse.toLocalEntity() = ProjectLocalEntity(
    id = id,
    orgId = orgId,
    managerId = managerId,
    name = name,
    description = description,
    startDate = startDate.toString(),
    endDate = endDate.toString(),
    status = status
)

