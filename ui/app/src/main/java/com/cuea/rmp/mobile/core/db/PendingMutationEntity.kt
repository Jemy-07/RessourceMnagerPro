package com.cuea.rmp.mobile.core.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_mutations")
data class PendingMutationEntity(
    @PrimaryKey val localId: String,
    val entityType: String,
    val httpMethod: String,
    val path: String,
    val bodyJson: String,
    val createdAt: Long,
    val status: PendingMutationStatus,
    val lastError: String? = null
)

enum class PendingMutationStatus {
    PENDING,
    IN_FLIGHT,
    SYNCED,
    FAILED
}

