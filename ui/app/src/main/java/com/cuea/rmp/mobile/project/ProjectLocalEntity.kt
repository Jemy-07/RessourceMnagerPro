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
    val status: String
)

