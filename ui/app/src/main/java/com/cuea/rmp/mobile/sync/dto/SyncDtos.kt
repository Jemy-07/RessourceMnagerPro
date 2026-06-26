package com.cuea.rmp.mobile.sync.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// Mirrors the backend's generic sync engine exactly (sync/domain/SyncEntry.java,
// sync/web/request/SyncEntryRequest.java, sync/application/dto/{SyncRow,ConflictInfo,
// PushResult,PullResult}.java) — one shared shape for every EntityType (RESOURCE,
// PROJECT, REQUEST, ...), not a per-domain DTO. `payload` is a generic field map
// (scalar JPA entity columns only — see SyncConfig.java's skills mixin for the one
// documented exception), so JsonElement rather than a typed class.

@Serializable
data class SyncEntryRequest(
    val entityType: String,
    val id: String,
    val payload: Map<String, JsonElement>,
    val clientUpdatedAt: String,
    val clientVersion: Long,
    val deleted: Boolean = false
)

@Serializable
data class SyncPushRequest(val changes: List<SyncEntryRequest>)

@Serializable
data class ConflictInfoResponse(
    val entityType: String,
    val id: String,
    val resolution: String, // CLIENT_WON | SERVER_WON
    val message: String
)

@Serializable
data class PushResultResponse(
    val appliedCount: Int,
    val conflictCount: Int,
    val conflicts: List<ConflictInfoResponse>
)

@Serializable
data class SyncRowResponse(
    val entityType: String,
    val id: String,
    val payload: Map<String, JsonElement>,
    val updatedAt: String,
    val version: Long,
    val deleted: Boolean
)

@Serializable
data class PullResultResponse(
    val serverTime: String,
    val count: Int,
    val changes: List<SyncRowResponse>
)
