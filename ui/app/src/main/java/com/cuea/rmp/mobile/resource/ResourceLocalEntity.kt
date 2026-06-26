package com.cuea.rmp.mobile.resource

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resources")
data class ResourceLocalEntity(
    @PrimaryKey val id: String,
    val orgId: String,
    val userId: String?,
    val name: String,
    val type: String,
    val hourlyRateAmount: Double,
    val currency: String,
    val availabilityStatus: String,
    val skillsSummary: String,
    // Sync-engine metadata (from /sync/pull's SyncRow, not the regular REST DTO — the
    // regular ResourceResponse has no version field). Needed as the clientVersion basis
    // for any offline edit queued through /sync/push; 0/unset until the first pull.
    val syncVersion: Long = 0,
    val serverUpdatedAt: String? = null,
    val pendingEdit: Boolean = false
)

