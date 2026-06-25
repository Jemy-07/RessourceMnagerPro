package com.cuea.rmp.mobile.timesheet

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(tableName = "timesheet_entries")
data class TimesheetLocalEntity(
    @PrimaryKey val id: String,
    val resourceId: String,
    val assignmentId: String,
    val workDate: LocalDate,
    val hours: Double,
    val syncState: LocalSyncState,
    val updatedAtMillis: Long
)

enum class LocalSyncState {
    PENDING_SYNC,
    SYNCED,
    FAILED
}

