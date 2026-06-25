package com.cuea.rmp.mobile.resource

import com.cuea.rmp.mobile.core.network.safeApiCall
import com.cuea.rmp.mobile.resource.dto.AvailabilityResponse
import com.cuea.rmp.mobile.resource.dto.ResourceMatchResponse
import com.cuea.rmp.mobile.resource.dto.ResourceResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResourceRepository @Inject constructor(
    private val resourceApi: ResourceApi,
    private val resourceDao: ResourceDao,
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

