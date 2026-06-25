package com.cuea.rmp.mobile.project

import com.cuea.rmp.mobile.core.network.safeApiCall
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

    suspend fun refreshProjects(pageSize: Int = 50) {
        val aggregated = mutableListOf<ProjectLocalEntity>()
        var page = 0
        var totalPages = 1

        while (page < totalPages) {
            val response = safeApiCall(json) {
                projectApi.listProjects(page = page, size = pageSize)
            }

            aggregated += response.content.map { item ->
                ProjectLocalEntity(
                    id = item.id,
                    orgId = item.orgId,
                    managerId = item.managerId,
                    name = item.name,
                    description = item.description,
                    startDate = item.startDate.toString(),
                    endDate = item.endDate.toString(),
                    status = item.status
                )
            }

            page += 1
            totalPages = response.totalPages
        }

        projectDao.clearAll()
        projectDao.upsertAll(aggregated)
    }
}

