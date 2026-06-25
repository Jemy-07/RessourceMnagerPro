package com.cuea.rmp.mobile.user

import com.cuea.rmp.mobile.core.network.safeApiCall
import com.cuea.rmp.mobile.user.dto.UpdateUserRequest
import com.cuea.rmp.mobile.user.dto.UserResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userApi: UserApi,
    private val userDao: UserDao,
    private val json: Json
) {

    fun observeUser(id: String): Flow<UserLocalEntity?> = userDao.observeById(id)

    suspend fun refreshUser(id: String) {
        val response = safeApiCall(json) { userApi.getUser(id) }
        userDao.upsertAll(listOf(response.toLocalEntity()))
    }

    suspend fun updateUser(id: String, request: UpdateUserRequest): UserResponse {
        val response = safeApiCall(json) { userApi.updateUser(id, request) }
        userDao.upsertAll(listOf(response.toLocalEntity()))
        return response
    }
}

private fun UserResponse.toLocalEntity() = UserLocalEntity(
    id = id,
    orgId = orgId,
    fullName = fullName,
    email = email,
    role = role,
    active = active
)
