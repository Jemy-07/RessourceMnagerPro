package com.cuea.rmp.mobile.sync

import androidx.room.Entity
import androidx.room.PrimaryKey

// No backend endpoint serves AuditLog to clients yet (it's an internal sync-conflict
// record written only from sync/application/usecase/PushChangesService.java). Fields
// mirror the backend's sync/domain/AuditLog.java domain class exactly, as a forward-looking
// scaffold for if/when a real AuditLog API is added — confirmed with product before building.
@Entity(tableName = "audit_logs")
data class AuditLogLocalEntity(
    @PrimaryKey val id: String,
    val entityType: String,
    val entityId: String,
    val action: String,
    val conflict: Boolean,
    val message: String,
    val occurredAt: String
)
