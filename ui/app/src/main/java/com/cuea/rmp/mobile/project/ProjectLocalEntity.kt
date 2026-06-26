package com.cuea.rmp.mobile.project

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectLocalEntity(
    @PrimaryKey val id: String,
    val orgId: String,
    val managerId: String,
    val name: String,
    val description: String?,
    val startDate: String,
    val endDate: String,
    val status: String,
    // Sync-engine metadata (from /sync/pull's SyncRow, not the regular REST DTO — the
    // regular ProjectResponse has no version field). Needed as the clientVersion basis
    // for any offline edit queued through /sync/push; 0/unset until the first pull.
    val syncVersion: Long = 0,
    val serverUpdatedAt: String? = null,
    val pendingEdit: Boolean = false
)

