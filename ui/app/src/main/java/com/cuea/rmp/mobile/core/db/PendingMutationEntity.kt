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
    val lastError: String? = null,
    // Set when the failure is a validation/business rejection (4xx with a parsed backend
    // message) rather than a transient network/server failure — classified at the call
    // site where the actual exception is available, not re-derived from lastError later.
    // Lets the UI show "fix this and retry" vs. "will retry automatically" instead of a
    // one-size-fits-all error (Cleanup Half-Sprint: failures used to be swallowed entirely).
    val permanentFailure: Boolean = false
)

enum class PendingMutationStatus {
    PENDING,
    IN_FLIGHT,
    SYNCED,
    FAILED
}

