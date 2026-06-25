package com.cuea.rmp.mobile.project

import com.cuea.rmp.mobile.core.network.safeApiCall
import com.cuea.rmp.mobile.project.dto.ProjectResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepository @Inject constructor(
    private val projectApi: ProjectApi,
    private val projectDao: ProjectDao,
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

