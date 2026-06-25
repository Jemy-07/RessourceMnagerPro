package com.cuea.rmp.mobile.resource

import com.cuea.rmp.mobile.core.network.safeApiCall
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

    suspend fun refreshResources(pageSize: Int = 50) {
        val aggregated = mutableListOf<ResourceLocalEntity>()
        var page = 0
        var totalPages = 1

        while (page < totalPages) {
            val response = safeApiCall(json) {
                resourceApi.listResources(page = page, size = pageSize)
            }

            aggregated += response.content.map { item ->
                ResourceLocalEntity(
                    id = item.id,
                    orgId = item.orgId,
                    userId = item.userId,
                    name = item.name,
                    type = item.type,
                    hourlyRateAmount = item.hourlyRateAmount,
                    currency = item.currency,
                    availabilityStatus = item.availabilityStatus,
                    skillsSummary = item.skills.joinToString(", ") { skill -> "${skill.skillId}:${skill.proficiency}" }
                )
            }

            page += 1
            totalPages = response.totalPages
        }

        resourceDao.clearAll()
        resourceDao.upsertAll(aggregated)
    }
}

