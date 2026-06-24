package com.cuea.rmp.mobile.notification

import com.cuea.rmp.mobile.core.network.safeApiCall
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationApi: NotificationApi,
    private val notificationDao: NotificationDao,
    private val json: Json
) {

    fun observeNotifications(): Flow<List<NotificationLocalEntity>> = notificationDao.observeAll()

    suspend fun refreshNotifications() {
        val notifications = safeApiCall(json) { notificationApi.listNotifications() }
        val local = notifications.map {
            NotificationLocalEntity(
                id = it.id,
                userId = it.userId,
                type = it.type,
                message = it.message,
                read = it.read
            )
        }

        notificationDao.clearAll()
        notificationDao.upsertAll(local)
    }

    suspend fun markRead(id: String) {
        safeApiCall(json) { notificationApi.markRead(id) }
        notificationDao.markRead(id)
    }
}


