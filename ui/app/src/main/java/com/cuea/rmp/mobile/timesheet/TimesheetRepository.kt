package com.cuea.rmp.mobile.timesheet

import com.cuea.rmp.mobile.core.db.PendingMutationDao
import com.cuea.rmp.mobile.core.db.PendingMutationEntity
import com.cuea.rmp.mobile.core.db.PendingMutationStatus
import com.cuea.rmp.mobile.core.network.ApiException
import com.cuea.rmp.mobile.core.network.safeApiCall
import com.cuea.rmp.mobile.timesheet.dto.LogTimeRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimesheetRepository @Inject constructor(
    private val timesheetApi: TimesheetApi,
    private val timesheetDao: TimesheetDao,
    private val pendingMutationDao: PendingMutationDao,
    private val json: Json
) {

    fun observeLocalTimesheets(): Flow<List<TimesheetLocalEntity>> = timesheetDao.observeAll()

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
                entityType = "TIMESHEET",
                httpMethod = "POST",
                path = "api/v1/timesheets",
                bodyJson = json.encodeToString(request),
                createdAt = now,
                status = PendingMutationStatus.PENDING
            )
        )

        // Best-effort immediate sync while online; worker retry covers offline path.
        runCatching { syncPendingTimesheets(limit = 1) }

        return id
    }

    suspend fun syncPendingTimesheets(limit: Int = 25) {
        val pending = pendingMutationDao.listByStatus(
            statuses = listOf(PendingMutationStatus.PENDING, PendingMutationStatus.FAILED),
            limit = limit
        )

        pending.filter { it.entityType == "TIMESHEET" }.forEach { mutation ->
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
                        lastError = apiException.message
                    )
                    timesheetDao.markFailed(mutation.localId, now)
                }
            }
        }
    }
}

