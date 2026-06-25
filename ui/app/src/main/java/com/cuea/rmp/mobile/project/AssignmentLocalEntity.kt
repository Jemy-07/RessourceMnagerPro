package com.cuea.rmp.mobile.project

import androidx.room.Entity
import androidx.room.PrimaryKey

// Fields mirror AssignmentResponse (project/dto/ProjectDtos.kt), the shape returned by
// AssignmentApi. The backend models "Task" and "Assignment" as the same concept
// (project/domain/Assignment.java — no separate Task entity), so this entity covers both.
@Entity(tableName = "assignments")
data class AssignmentLocalEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val resourceId: String,
    val title: String,
    val startDate: String,
    val endDate: String,
    val allocationPct: Int,
    val status: String
)
