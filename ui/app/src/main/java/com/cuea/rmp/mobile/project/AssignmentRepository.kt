package com.cuea.rmp.mobile.project

import com.cuea.rmp.mobile.core.network.safeApiCall
import com.cuea.rmp.mobile.project.dto.AssignResourceRequest
import com.cuea.rmp.mobile.project.dto.AssignmentResponse
import com.cuea.rmp.mobile.project.dto.UpdateAssignmentRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

// The backend splits assignment operations across two controllers: creation and
// per-project listing live on ProjectApi (project/domain/Assignment.java is created
// through the owning Project's AssignResourceUseCase), while get-by-id and update
// live on AssignmentApi. This repository fronts both with one local cache.
@Singleton
class AssignmentRepository @Inject constructor(
    private val assignmentApi: AssignmentApi,
    private val projectApi: ProjectApi,
    private val assignmentDao: AssignmentDao,
    private val json: Json
) {

    fun observeAssignmentsForProject(projectId: String): Flow<List<AssignmentLocalEntity>> =
        assignmentDao.observeByProject(projectId)

    fun observeAssignmentsForResource(resourceId: String): Flow<List<AssignmentLocalEntity>> =
        assignmentDao.observeByResource(resourceId)

    fun observeUpcomingAssignments(limit: Int = 5): Flow<List<AssignmentLocalEntity>> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        return assignmentDao.observeUpcoming(today, limit)
    }

    suspend fun refreshAssignmentsForProject(projectId: String) {
        val assignments = safeApiCall(json) { projectApi.getAssignmentsByProject(projectId) }
        assignmentDao.clearByProject(projectId)
        assignmentDao.upsertAll(assignments.map { it.toLocalEntity() })
    }

    suspend fun createAssignment(projectId: String, request: AssignResourceRequest): AssignmentResponse {
        val response = safeApiCall(json) { projectApi.assignResource(projectId, request) }
        assignmentDao.upsertAll(listOf(response.toLocalEntity()))
        return response
    }

    suspend fun updateAssignment(id: String, request: UpdateAssignmentRequest): AssignmentResponse {
        val response = safeApiCall(json) { assignmentApi.updateAssignment(id, request) }
        assignmentDao.upsertAll(listOf(response.toLocalEntity()))
        return response
    }

    suspend fun getAssignment(id: String): AssignmentResponse =
        safeApiCall(json) { assignmentApi.getAssignment(id) }
}

private fun AssignmentResponse.toLocalEntity() = AssignmentLocalEntity(
    id = id,
    projectId = projectId,
    resourceId = resourceId,
    title = title,
    startDate = startDate.toString(),
    endDate = endDate.toString(),
    allocationPct = allocationPct,
    status = status
)
