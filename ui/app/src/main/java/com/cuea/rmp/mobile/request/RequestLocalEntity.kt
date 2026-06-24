package com.cuea.rmp.mobile.request

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "requests")
data class RequestLocalEntity(
    @PrimaryKey val id: String,
    val requesterId: String,
    val approverId: String?,
    val resourceId: String,
    val projectId: String,
    val title: String,
    val startDate: String,
    val endDate: String,
    val allocationPct: Int,
    val status: String,
    val comments: String?,
    val decidedAt: String?
)

