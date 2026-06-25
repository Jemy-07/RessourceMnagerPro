package com.cuea.rmp.mobile.request

import com.cuea.rmp.mobile.core.network.safeApiCall
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestRepository @Inject constructor(
    private val requestApi: RequestApi,
    private val requestDao: RequestDao,
    private val json: Json
) {

    fun observeRequests(): Flow<List<RequestLocalEntity>> = requestDao.observeAll()

    suspend fun refreshRequests(status: String? = null) {
        val requests = safeApiCall(json) {
            requestApi.listRequests(status = status)
        }

        val local = requests.map { item ->
            RequestLocalEntity(
                id = item.id,
                requesterId = item.requesterId,
                approverId = item.approverId,
                resourceId = item.resourceId,
                projectId = item.projectId,
                title = item.title,
                startDate = item.startDate.toString(),
                endDate = item.endDate.toString(),
                allocationPct = item.allocationPct,
                status = item.status,
                comments = item.comments,
                decidedAt = item.decidedAt?.toString()
            )
        }

        requestDao.clearAll()
        requestDao.upsertAll(local)
    }

    suspend fun approve(id: String) {
        safeApiCall(json) { requestApi.approveRequest(id) }
    }

    suspend fun reject(id: String, comments: String) {
        safeApiCall(json) { requestApi.rejectRequest(id, com.cuea.rmp.mobile.request.dto.RejectRequestRequest(comments)) }
    }
}

