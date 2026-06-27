package com.cuea.rmp.mobile.request

import com.cuea.rmp.mobile.core.db.PendingMutationDao
import com.cuea.rmp.mobile.core.db.PendingMutationEntity
import com.cuea.rmp.mobile.core.db.PendingMutationStatus
import com.cuea.rmp.mobile.core.network.safeApiCall
import com.cuea.rmp.mobile.request.dto.CreateRequestRequest
import com.cuea.rmp.mobile.sync.SyncFailureUi
import com.cuea.rmp.mobile.sync.isPermanentSyncFailure
import com.cuea.rmp.mobile.sync.toFailureUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val ENTITY_TYPE = "REQUEST"

@Singleton
class RequestRepository @Inject constructor(
    private val requestApi: RequestApi,
    private val requestDao: RequestDao,
    private val pendingMutationDao: PendingMutationDao,
    private val json: Json
) {

    fun observeRequests(): Flow<List<RequestLocalEntity>> = requestDao.observeAll()

    /** Sync-relevant failures for queued request creates — see [SyncFailureUi]. */
    fun observeSyncFailures(): Flow<List<SyncFailureUi>> = pendingMutationDao.observeAll().map { list ->
        list.filter { it.entityType == ENTITY_TYPE && it.status == PendingMutationStatus.FAILED }
            .map { it.toFailureUi() }
    }

    suspend fun refreshRequests(status: String? = null) {
        val requests = safeApiCall(json) {
            requestApi.listRequests(status = status)
        }

        val serverRows = requests.map { item ->
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

        // Local optimistic-create rows whose mutation hasn't reached the server yet
        // (still PENDING, IN_FLIGHT, or permanently FAILED) have no counterpart in the
        // server list and must survive this refresh — a plain clear+replace silently
        // deleted them (failure card included) the moment ANY refresh ran, including the
        // implicit one in init{} (Cleanup Half-Sprint #2). Once a mutation reaches SYNCED,
        // its placeholder row is naturally superseded by the server's row for that same
        // create (a different id) and is safe to drop here.
        val unsyncedLocalIds = pendingMutationDao.listByStatus(
            statuses = listOf(PendingMutationStatus.PENDING, PendingMutationStatus.IN_FLIGHT, PendingMutationStatus.FAILED),
            limit = Int.MAX_VALUE
        ).filter { it.entityType == ENTITY_TYPE }.map { it.localId }

        requestDao.deleteAllExcept(serverRows.map { it.id } + unsyncedLocalIds)
        requestDao.upsertAll(serverRows)
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
        allocationPct: Int,
        requesterId: String?
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
                    requesterId = requesterId.orEmpty(),
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
                entityType = ENTITY_TYPE,
                httpMethod = "POST",
                path = "api/v1/requests",
                bodyJson = json.encodeToString(request),
                createdAt = now,
                status = PendingMutationStatus.PENDING
            )
        )

        // Targets this specific mutation rather than "whatever's oldest" — see
        // syncMutation's doc comment for why (head-of-line blocking, Cleanup Half-Sprint).
        runCatching { syncMutation(localId) }

        return localId
    }

    /** Processes the full queue in createdAt order — used by the periodic/background worker. */
    suspend fun syncPendingRequests(limit: Int = 25) {
        val pending = pendingMutationDao.listByStatus(
            statuses = listOf(PendingMutationStatus.PENDING, PendingMutationStatus.FAILED),
            limit = limit
        ).filter { it.entityType == ENTITY_TYPE }

        if (pending.isEmpty()) return

        var anySynced = false
        pending.forEach { mutation -> if (syncOne(mutation)) anySynced = true }

        // Replaces the optimistic local placeholder with the server-assigned row.
        if (anySynced) {
            runCatching { refreshRequests() }
        }
    }

    // Targets exactly one mutation by id — used right after a create, in the same coroutine,
    // instead of relying on the FIFO full-queue path above. Without this, a brand-new
    // request's own "sync now" attempt could be diverted into retrying an older, already
    // -stuck mutation (confirmed live: an older RESOURCE_UNAVAILABLE-rejected request
    // delayed a new, unrelated one's sync until the next full refresh).
    suspend fun syncMutation(localId: String) {
        val mutation = pendingMutationDao.getById(localId)
            ?.takeIf { it.entityType == ENTITY_TYPE && it.status != PendingMutationStatus.SYNCED }
            ?: return
        if (syncOne(mutation)) {
            runCatching { refreshRequests() }
        }
    }

    /** Returns true if this mutation synced successfully. */
    private suspend fun syncOne(mutation: PendingMutationEntity): Boolean {
        pendingMutationDao.updateStatus(mutation.localId, PendingMutationStatus.IN_FLIGHT)
        val request = json.decodeFromString<CreateRequestRequest>(mutation.bodyJson)

        return try {
            safeApiCall(json) { requestApi.createRequest(request) }
            pendingMutationDao.updateStatus(mutation.localId, PendingMutationStatus.SYNCED)
            true
        } catch (throwable: Throwable) {
            // Catches plain IOException (genuinely offline / no network) as well as
            // ApiException (server rejected it) — narrowing this to ApiException only
            // left a mutation stuck at IN_FLIGHT forever when the device had no
            // connectivity at all, since IN_FLIGHT is never re-queried by listByStatus.
            pendingMutationDao.updateStatus(
                localId = mutation.localId,
                status = PendingMutationStatus.FAILED,
                lastError = throwable.message,
                permanentFailure = throwable.isPermanentSyncFailure()
            )
            false
        }
    }
}

