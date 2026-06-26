package com.cuea.rmp.mobile.request

import com.cuea.rmp.mobile.auth.TokenManager
import com.cuea.rmp.mobile.core.db.PendingMutationDao
import com.cuea.rmp.mobile.core.db.PendingMutationEntity
import com.cuea.rmp.mobile.core.db.PendingMutationStatus
import com.cuea.rmp.mobile.core.network.ApiException
import com.cuea.rmp.mobile.core.network.safeApiCall
import com.cuea.rmp.mobile.request.dto.CreateRequestRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestRepository @Inject constructor(
    private val requestApi: RequestApi,
    private val requestDao: RequestDao,
    private val pendingMutationDao: PendingMutationDao,
    private val tokenManager: TokenManager,
    private val json: Json
) {

    fun observeRequests(): Flow<List<RequestLocalEntity>> = requestDao.observeAll()

    suspend fun refreshRequests(status: String? = null) {
        val requests = safeApiCall(json) {
            requestApi.listRequests(status = status)
        }

        val local = requests.map { item ->
            RequestLocalEntity(
                id = item.id,
                requesterId = item.requesterId,
                approverId = item.approverId,
                resourceId = item.resourceId,
                projectId = item.projectId,
                title = item.title,
                startDate = item.startDate.toString(),
                endDate = item.endDate.toString(),
                allocationPct = item.allocationPct,
                status = item.status,
                comments = item.comments,
                decidedAt = item.decidedAt?.toString()
            )
        }

        requestDao.clearAll()
        requestDao.upsertAll(local)
    }

    suspend fun approve(id: String) {
        safeApiCall(json) { requestApi.approveRequest(id) }
    }

    suspend fun reject(id: String, comments: String) {
        safeApiCall(json) { requestApi.rejectRequest(id, com.cuea.rmp.mobile.request.dto.RejectRequestRequest(comments)) }
    }

    // Offline create: visible to MEMBER and up (RequestController.create/list are
    // isAuthenticated()-only, unlike Resource/Project's ADMIN/MANAGER-gated writes — see
    // Sprint 3.5 RBAC audit). Same REST-replay queue pattern as TimesheetRepository.logTime:
    // a create has nothing to conflict against, so there's no need for the generic
    // /sync/push engine SyncRepository uses for Resource/Project edits.
    //
    // Known gap (flagged, not fixed here): unlike LogTimeRequest, CreateRequestRequest has
    // no client-supplied id, so the backend always mints a new id — if a replay is retried
    // after a request that actually succeeded server-side (e.g. the response was lost but
    // not the write), this can create a duplicate request. Out of scope for this sprint.
    suspend fun createRequestOffline(
        resourceId: String,
        projectId: String,
        title: String,
        startDate: String,
        endDate: String,
        allocationPct: Int
    ): String {
        val localId = UUID.randomUUID().toString()
        val now = Clock.System.now().toEpochMilliseconds()

        val request = CreateRequestRequest(
            resourceId = resourceId,
            projectId = projectId,
            title = title,
            startDate = LocalDate.parse(startDate),
            endDate = LocalDate.parse(endDate),
            allocationPct = allocationPct
        )

        requestDao.upsertAll(
            listOf(
                RequestLocalEntity(
                    id = localId,
                    requesterId = tokenManager.getCurrentUserId().orEmpty(),
                    approverId = null,
                    resourceId = resourceId,
                    projectId = projectId,
                    title = title,
                    startDate = startDate,
                    endDate = endDate,
                    allocationPct = allocationPct,
                    status = "PENDING",
                    comments = null,
                    decidedAt = null
                )
            )
        )

        pendingMutationDao.upsert(
            PendingMutationEntity(
                localId = localId,
                entityType = "REQUEST",
                httpMethod = "POST",
                path = "api/v1/requests",
                bodyJson = json.encodeToString(request),
                createdAt = now,
                status = PendingMutationStatus.PENDING
            )
        )

        // Best-effort immediate sync while online; worker retry covers offline path.
        runCatching { syncPendingRequests(limit = 1) }

        return localId
    }

    suspend fun syncPendingRequests(limit: Int = 25) {
        val pending = pendingMutationDao.listByStatus(
            statuses = listOf(PendingMutationStatus.PENDING, PendingMutationStatus.FAILED),
            limit = limit
        ).filter { it.entityType == "REQUEST" }

        if (pending.isEmpty()) return

        var anySynced = false
        pending.forEach { mutation ->
            pendingMutationDao.updateStatus(mutation.localId, PendingMutationStatus.IN_FLIGHT)
            val request = json.decodeFromString<CreateRequestRequest>(mutation.bodyJson)

            try {
                safeApiCall(json) { requestApi.createRequest(request) }
                pendingMutationDao.updateStatus(mutation.localId, PendingMutationStatus.SYNCED)
                anySynced = true
            } catch (apiException: ApiException) {
                pendingMutationDao.updateStatus(
                    localId = mutation.localId,
                    status = PendingMutationStatus.FAILED,
                    lastError = apiException.message
                )
            }
        }

        // Replaces the optimistic local placeholder with the server-assigned row.
        if (anySynced) {
            runCatching { refreshRequests() }
        }
    }
}

