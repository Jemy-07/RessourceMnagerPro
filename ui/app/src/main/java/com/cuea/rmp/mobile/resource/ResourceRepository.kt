package com.cuea.rmp.mobile.resource

import com.cuea.rmp.mobile.core.db.PendingMutationDao
import com.cuea.rmp.mobile.core.db.PendingMutationEntity
import com.cuea.rmp.mobile.core.db.PendingMutationStatus
import com.cuea.rmp.mobile.core.network.safeApiCall
import com.cuea.rmp.mobile.resource.dto.AvailabilityResponse
import com.cuea.rmp.mobile.resource.dto.ResourceMatchResponse
import com.cuea.rmp.mobile.resource.dto.ResourceResponse
import com.cuea.rmp.mobile.sync.dto.SyncEntryRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResourceRepository @Inject constructor(
    private val resourceApi: ResourceApi,
    private val resourceDao: ResourceDao,
    private val pendingMutationDao: PendingMutationDao,
    private val json: Json
) {

    fun observeResources(): Flow<List<ResourceLocalEntity>> = resourceDao.observeAll()

    fun observeResource(id: String): Flow<ResourceLocalEntity?> = resourceDao.observeById(id)

    suspend fun refreshResources(pageSize: Int = 50) {
        val aggregated = mutableListOf<ResourceLocalEntity>()
        var page = 0
        var totalPages = 1

        while (page < totalPages) {
            val response = safeApiCall(json) {
                resourceApi.listResources(page = page, size = pageSize)
            }

            aggregated += response.content.map { it.toLocalEntity() }

            page += 1
            totalPages = response.totalPages
        }

        resourceDao.clearAll()
        resourceDao.upsertAll(aggregated)
    }

    /** Targeted single-resource refresh for Resource Detail — avoids re-pulling the full list. */
    suspend fun refreshResource(id: String) {
        val response = safeApiCall(json) { resourceApi.getResource(id) }
        resourceDao.upsertAll(listOf(response.toLocalEntity()))
    }

    suspend fun matchResources(skillId: String, from: String, to: String): List<ResourceMatchResponse> =
        safeApiCall(json) { resourceApi.matchResources(skillId = skillId, from = from, to = to) }

    // No backend endpoint exposes a resource's time-off list to clients at all
    // (resource/domain/TimeOff.java is internal to AvailabilityChecker/MatchResourcesService,
    // no controller serves it) — this date-range check is the closest available substitute.
    suspend fun checkAvailability(id: String, from: String, to: String): AvailabilityResponse =
        safeApiCall(json) { resourceApi.checkAvailability(id = id, from = from, to = to) }

    suspend fun applySyncMetadata(id: String, version: Long, updatedAt: String) {
        resourceDao.applySyncMetadata(id, version, updatedAt)
    }

    // Offline edit: writes the local cache immediately and queues the change through the
    // generic sync engine (/sync/push, not the regular PUT — that endpoint applies
    // unconditionally with no version check at all, so it can't detect or report a
    // concurrent edit the way the sync engine does). clientVersion is whatever was last
    // learned from /sync/pull (see refreshSyncMetadata) — 0 if this resource was never
    // pulled, which is only safe if nobody has edited it since it was created.
    /** Returns the queued mutation's localId — callers use it to target an immediate push. */
    suspend fun editResourceOffline(
        id: String,
        name: String,
        hourlyRateAmount: Double,
        currency: String,
        availabilityStatus: String
    ): String {
        val current = resourceDao.observeById(id).first()
            ?: error("Resource $id is not cached locally — refresh before editing")
        val now = Clock.System.now()

        resourceDao.applyLocalEdit(
            id = id,
            name = name,
            hourlyRateAmount = hourlyRateAmount,
            currency = currency,
            availabilityStatus = availabilityStatus
        )

        val entry = SyncEntryRequest(
            entityType = "RESOURCE",
            id = id,
            payload = mapOf(
                "name" to JsonPrimitive(name),
                "hourlyRateAmount" to JsonPrimitive(hourlyRateAmount),
                "hourlyRateCurrency" to JsonPrimitive(currency),
                "availabilityStatus" to JsonPrimitive(availabilityStatus)
            ),
            clientUpdatedAt = now.toString(),
            clientVersion = current.syncVersion
        )

        val localId = "RESOURCE:$id"
        pendingMutationDao.upsert(
            PendingMutationEntity(
                localId = localId,
                entityType = "RESOURCE",
                httpMethod = "POST",
                path = "api/v1/sync/push",
                bodyJson = json.encodeToString(entry),
                createdAt = now.toEpochMilliseconds(),
                status = PendingMutationStatus.PENDING
            )
        )
        return localId
    }
}

private fun ResourceResponse.toLocalEntity() = ResourceLocalEntity(
    id = id,
    orgId = orgId,
    userId = userId,
    name = name,
    type = type,
    hourlyRateAmount = hourlyRateAmount,
    currency = currency,
    availabilityStatus = availabilityStatus,
    skillsSummary = skills.joinToString(", ") { skill -> "${skill.skillId}:${skill.proficiency}" }
)

