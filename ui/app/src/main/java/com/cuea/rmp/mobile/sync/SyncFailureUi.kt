package com.cuea.rmp.mobile.sync

import com.cuea.rmp.mobile.core.db.PendingMutationEntity
import com.cuea.rmp.mobile.core.network.ApiException

/**
 * UI-facing view of a mutation that failed to sync — see [PendingMutationEntity.lastError]/
 * [PendingMutationEntity.permanentFailure] for the stored shape. Replaces the
 * Sprint 4 behavior where a failed push was recorded in Room but never read by any UI.
 */
data class SyncFailureUi(val localId: String, val message: String, val isPermanent: Boolean)

fun PendingMutationEntity.toFailureUi(): SyncFailureUi = SyncFailureUi(
    localId = localId,
    message = lastError ?: "Sync failed for an unknown reason",
    isPermanent = permanentFailure
)

/**
 * A 4xx with a parsed backend message is a validation/business rejection the user can act
 * on (wrong enum value, resource unavailable, etc.) — retrying as-is will fail again. A
 * 5xx, a missing status code (network-level failure before any HTTP response), or any
 * other throwable is presumed transient: the existing retry path (worker / manual retry)
 * is expected to eventually succeed without the user changing anything.
 *
 * The backend's error responses don't carry a finer-grained "permanent vs transient" flag
 * of their own — this 4xx/other split is the best signal available without guessing at
 * intent per errorCode.
 */
fun Throwable.isPermanentSyncFailure(): Boolean =
    this is ApiException && statusCode != null && statusCode in 400..499
