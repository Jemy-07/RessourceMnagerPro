package com.cuea.rmp.mobile.timesheet

import com.cuea.rmp.mobile.core.db.PendingMutationDao
import com.cuea.rmp.mobile.core.db.PendingMutationEntity
import com.cuea.rmp.mobile.core.db.PendingMutationStatus
import com.cuea.rmp.mobile.core.network.ApiException
import com.cuea.rmp.mobile.core.network.safeApiCall
import com.cuea.rmp.mobile.sync.SyncFailureUi
import com.cuea.rmp.mobile.sync.isPermanentSyncFailure
import com.cuea.rmp.mobile.sync.toFailureUi
import com.cuea.rmp.mobile.timesheet.dto.LogTimeRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val ENTITY_TYPE = "TIMESHEET"

@Singleton
class TimesheetRepository @Inject constructor(
    private val timesheetApi: TimesheetApi,
    private val timesheetDao: TimesheetDao,
    private val pendingMutationDao: PendingMutationDao,
    private val json: Json
) {

    fun observeLocalTimesheets(): Flow<List<TimesheetLocalEntity>> = timesheetDao.observeAll()

    /** Sync-relevant failures for queued timesheet entries — see [SyncFailureUi]. */
    fun observeSyncFailures(): Flow<List<SyncFailureUi>> = pendingMutationDao.observeAll().map { list ->
        list.filter { it.entityType == ENTITY_TYPE && it.status == PendingMutationStatus.FAILED }
            .map { it.toFailureUi() }
    }

    suspend fun logTime(
        resourceId: String,
        assignmentId: String,
        workDate: LocalDate,
        hours: Double
    ): String {
        val id = UUID.randomUUID().toString()
        val now = Clock.System.now().toEpochMilliseconds()

        val request = LogTimeRequest(
            id = id,
            resourceId = resourceId,
            assignmentId = assignmentId,
            workDate = workDate,
            hours = hours
        )

        timesheetDao.upsert(
            TimesheetLocalEntity(
                id = id,
                resourceId = resourceId,
                assignmentId = assignmentId,
                workDate = workDate,
                hours = hours,
                syncState = LocalSyncState.PENDING_SYNC,
                updatedAtMillis = now
            )
        )

        pendingMutationDao.upsert(
            PendingMutationEntity(
                localId = id,
                entityType = ENTITY_TYPE,
                httpMethod = "POST",
                path = "api/v1/timesheets",
                bodyJson = json.encodeToString(request),
                createdAt = now,
                status = PendingMutationStatus.PENDING
            )
        )

        // Targets this specific mutation rather than "whatever's oldest" — see
        // syncMutation's doc comment for why (head-of-line blocking, Cleanup Half-Sprint).
        runCatching { syncMutation(id) }

        return id
    }

    /** Processes the full queue in createdAt order — used by the periodic/background worker. */
    suspend fun syncPendingTimesheets(limit: Int = 25) {
        val pending = pendingMutationDao.listByStatus(
            statuses = listOf(PendingMutationStatus.PENDING, PendingMutationStatus.FAILED),
            limit = limit
        )

        pending.filter { it.entityType == ENTITY_TYPE }.forEach { mutation -> syncOne(mutation) }
    }

    // Targets exactly one mutation by id — used right after logging time, in the same
    // coroutine, instead of relying on the FIFO full-queue path above. Without this, a
    // brand-new entry's own "sync now" attempt could be diverted into retrying an older,
    // already-stuck mutation (same head-of-line bug confirmed live for Request).
    suspend fun syncMutation(localId: String) {
        val mutation = pendingMutationDao.getById(localId)
            ?.takeIf { it.entityType == ENTITY_TYPE && it.status != PendingMutationStatus.SYNCED }
            ?: return
        syncOne(mutation)
    }

    private suspend fun syncOne(mutation: PendingMutationEntity) {
        pendingMutationDao.updateStatus(mutation.localId, PendingMutationStatus.IN_FLIGHT)

        val request = json.decodeFromString<LogTimeRequest>(mutation.bodyJson)
        val now = Clock.System.now().toEpochMilliseconds()

        try {
            safeApiCall(json) {
                timesheetApi.logTime(request)
            }

            pendingMutationDao.updateStatus(mutation.localId, PendingMutationStatus.SYNCED)
            timesheetDao.markSynced(mutation.localId, now)
        } catch (apiException: ApiException) {
            if (apiException.code == "TIMESHEET_EXISTS") {
                pendingMutationDao.updateStatus(mutation.localId, PendingMutationStatus.SYNCED)
                timesheetDao.markSynced(mutation.localId, now)
            } else {
                pendingMutationDao.updateStatus(
                    localId = mutation.localId,
                    status = PendingMutationStatus.FAILED,
                    lastError = apiException.message,
                    permanentFailure = apiException.isPermanentSyncFailure()
                )
                timesheetDao.markFailed(mutation.localId, now)
            }
        } catch (throwable: Throwable) {
            // Plain IOException (genuinely offline / no network) was previously
            // uncaught here, leaving the mutation stuck at IN_FLIGHT forever since
            // listByStatus never re-queries that status — found via the analogous
            // RequestRepository test in Sprint 4.
            pendingMutationDao.updateStatus(
                localId = mutation.localId,
                status = PendingMutationStatus.FAILED,
                lastError = throwable.message,
                permanentFailure = throwable.isPermanentSyncFailure()
            )
            timesheetDao.markFailed(mutation.localId, now)
        }
    }
}

