package com.cuea.rmp.mobile.sync

import com.cuea.rmp.mobile.core.db.PendingMutationDao
import com.cuea.rmp.mobile.core.db.PendingMutationEntity
import com.cuea.rmp.mobile.core.db.PendingMutationStatus
import com.cuea.rmp.mobile.core.network.safeApiCall
import com.cuea.rmp.mobile.project.ProjectRepository
import com.cuea.rmp.mobile.resource.ResourceRepository
import com.cuea.rmp.mobile.sync.dto.ConflictInfoResponse
import com.cuea.rmp.mobile.sync.dto.SyncEntryRequest
import com.cuea.rmp.mobile.sync.dto.SyncPushRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pushes/pulls through the backend's GENERIC sync engine (`/sync/push`, `/sync/pull` —
 * entityType-discriminated, optimistic-lock-aware, real last-write-wins conflict
 * resolution). This is distinct from the REST-replay queue pattern Timesheet/Request use
 * (see [com.cuea.rmp.mobile.timesheet.TimesheetRepository]): those are pure creates with
 * nothing to conflict against, so replaying the original domain endpoint is sufficient.
 * Resource/Project EDITS can race another session's edit to the same row, and only this
 * engine — not the regular `PUT` endpoints, which apply unconditionally with no version
 * check at all (confirmed by reading UpdateResourceService/UpdateProjectService) —
 * detects and reports that.
 */
private val SYNC_ENGINE_ENTITY_TYPES = setOf("RESOURCE", "PROJECT")

/** UI-facing view of a recorded conflict outcome — see AuditLogLocalEntity for the stored shape. */
data class ConflictUi(val resolution: String, val message: String, val occurredAt: String)

@Singleton
class SyncRepository @Inject constructor(
    private val syncApi: SyncApi,
    private val pendingMutationDao: PendingMutationDao,
    private val auditLogDao: AuditLogDao,
    private val resourceRepository: ResourceRepository,
    private val projectRepository: ProjectRepository,
    private val json: Json
) {

    fun observeConflicts(): Flow<List<AuditLogLocalEntity>> = auditLogDao.observeAll()

    /** Sync-relevant failures across Resource/Project edits — see [SyncFailureUi]. */
    fun observeSyncFailures(): Flow<List<SyncFailureUi>> = pendingMutationDao.observeAll().map { list ->
        list.filter { it.entityType in SYNC_ENGINE_ENTITY_TYPES && it.status == PendingMutationStatus.FAILED }
            .map { it.toFailureUi() }
    }

    /** Refreshes the local clientVersion/serverUpdatedAt basis for offline edits to build on. */
    suspend fun refreshSyncMetadata() {
        val pull = safeApiCall(json) { syncApi.pull(since = null) }
        pull.changes.forEach { row ->
            when (row.entityType) {
                "RESOURCE" -> resourceRepository.applySyncMetadata(row.id, row.version, row.updatedAt)
                "PROJECT" -> projectRepository.applySyncMetadata(row.id, row.version, row.updatedAt)
            }
        }
    }

    /** Processes the full queue in createdAt order — used by the periodic/background worker. */
    suspend fun pushPendingMutations(limit: Int = 25) {
        val pending = pendingMutationDao.listByStatus(
            statuses = listOf(PendingMutationStatus.PENDING, PendingMutationStatus.FAILED),
            limit = limit
        ).filter { it.entityType in SYNC_ENGINE_ENTITY_TYPES }

        if (pending.isEmpty()) return
        pushBatch(pending)
    }

    // Targets exactly one mutation by id — used right after a save, in the same coroutine
    // as the edit, instead of relying on the FIFO full-queue path above. Without this, a
    // new edit's own "sync now" attempt could be diverted into retrying an older, already
    // -stuck mutation (head-of-line blocking — confirmed live in the Cleanup Half-Sprint),
    // since the full-queue query always picks oldest-first.
    suspend fun pushMutation(localId: String) {
        val mutation = pendingMutationDao.getById(localId)
            ?.takeIf { it.entityType in SYNC_ENGINE_ENTITY_TYPES && it.status != PendingMutationStatus.SYNCED }
            ?: return
        pushBatch(listOf(mutation))
    }

    private suspend fun pushBatch(pending: List<PendingMutationEntity>) {
        pending.forEach { pendingMutationDao.updateStatus(it.localId, PendingMutationStatus.IN_FLIGHT) }

        val entries = pending.associateWith { json.decodeFromString<SyncEntryRequest>(it.bodyJson) }

        try {
            val result = safeApiCall(json) {
                syncApi.push(SyncPushRequest(changes = entries.values.toList()))
            }
            val conflictsByKey = result.conflicts.associateBy { it.entityType to it.id }

            entries.forEach { (mutation, entry) ->
                val conflict = conflictsByKey[entry.entityType to entry.id]
                if (conflict != null) {
                    recordConflict(conflict)
                }
                pendingMutationDao.updateStatus(mutation.localId, PendingMutationStatus.SYNCED)
            }

            // Refresh affected rows from the server: picks up the bumped version after a
            // CLIENT_WON apply, and reverts local content after a SERVER_WON rejection
            // (the local cache otherwise keeps showing the user's discarded edit).
            entries.values.forEach { entry ->
                when (entry.entityType) {
                    "RESOURCE" -> runCatching { resourceRepository.refreshResource(entry.id) }
                    "PROJECT" -> runCatching { projectRepository.refreshProject(entry.id) }
                }
            }
            refreshSyncMetadata()
        } catch (throwable: Throwable) {
            // NOTE: this is an all-or-nothing batch — the backend applies the whole
            // /sync/push call in one @Transactional method, so ONE malformed entry (e.g. a
            // bad enum value) rolls back and fails every other entry in the same call too.
            // pushMutation() above sends single-entry batches specifically to avoid one bad
            // edit poisoning unrelated ones; pushPendingMutations()'s full-queue batch can
            // still be poisoned this way — flagged, not fixed here (out of scope: the brief
            // says the periodic worker keeps processing the full queue as-is).
            val permanent = throwable.isPermanentSyncFailure()
            pending.forEach {
                pendingMutationDao.updateStatus(
                    it.localId,
                    PendingMutationStatus.FAILED,
                    lastError = throwable.message,
                    permanentFailure = permanent
                )
            }
            throw throwable
        }
    }

    private suspend fun recordConflict(conflict: ConflictInfoResponse) {
        auditLogDao.upsertAll(
            listOf(
                AuditLogLocalEntity(
                    id = UUID.randomUUID().toString(),
                    entityType = conflict.entityType,
                    entityId = conflict.id,
                    action = conflict.resolution,
                    conflict = true,
                    message = conflict.message,
                    occurredAt = Clock.System.now().toString()
                )
            )
        )
    }
}
