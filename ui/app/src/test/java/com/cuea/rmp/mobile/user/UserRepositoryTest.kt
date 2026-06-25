package com.cuea.rmp.mobile.user

import com.cuea.rmp.mobile.core.network.ApiResponse
import com.cuea.rmp.mobile.core.network.PageResult
import com.cuea.rmp.mobile.user.dto.CreateUserRequest
import com.cuea.rmp.mobile.user.dto.UpdateUserRequest
import com.cuea.rmp.mobile.user.dto.UserResponse
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test

private class FakeUserDao : UserDao {
    val rows = MutableStateFlow<List<UserLocalEntity>>(emptyList())

    override fun observeAll(): Flow<List<UserLocalEntity>> = rows
    override fun observeById(id: String): Flow<UserLocalEntity?> = rows.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun upsertAll(users: List<UserLocalEntity>) {
        val byId = rows.value.associateBy { it.id }.toMutableMap()
        users.forEach { byId[it.id] = it }
        rows.value = byId.values.toList()
    }

    override suspend fun clearAll() {
        rows.value = emptyList()
    }
}

private class FakeUserApi(
    private val onGet: (String) -> UserResponse = { error("not stubbed") },
    private val onUpdate: (String, UpdateUserRequest) -> UserResponse = { _, _ -> error("not stubbed") },
    private val failGetWith: Throwable? = null
) : UserApi {
    override suspend fun createUser(request: CreateUserRequest): ApiResponse<UserResponse> = error("not used")
    override suspend fun listUsers(page: Int, size: Int): ApiResponse<PageResult<UserResponse>> = error("not used")
    override suspend fun deactivateUser(id: String): ApiResponse<Unit> = error("not used")

    override suspend fun getUser(id: String): ApiResponse<UserResponse> {
        failGetWith?.let { throw it }
        return ApiResponse(success = true, data = onGet(id))
    }

    override suspend fun updateUser(id: String, request: UpdateUserRequest): ApiResponse<UserResponse> =
        ApiResponse(success = true, data = onUpdate(id, request))
}

class UserRepositoryTest {

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false; isLenient = true }

    @Test
    fun `refresh caches the user returned by the backend`() = runTest {
        val dao = FakeUserDao()
        val response = UserResponse(id = "u1", orgId = "org1", fullName = "Ada Lovelace", email = "ada@example.com", role = "ADMIN", active = true)
        val api = FakeUserApi(onGet = { response })
        val repository = UserRepository(api, dao, json)

        repository.refreshUser("u1")

        val cached = dao.observeById("u1").first()
        assertEquals("Ada Lovelace", cached?.fullName)
        assertEquals("ADMIN", cached?.role)
    }

    @Test
    fun `refresh failure leaves no user cached`() = runTest {
        val dao = FakeUserDao()
        val api = FakeUserApi(failGetWith = IOException("offline"))
        val repository = UserRepository(api, dao, json)

        try {
            repository.refreshUser("u1")
            fail("expected IOException to propagate")
        } catch (expected: IOException) {
            // expected
        }

        assertNull(dao.observeById("u1").first())
    }

    @Test
    fun `updateUser persists the backend's updated fields`() = runTest {
        val dao = FakeUserDao()
        dao.rows.value = listOf(
            UserLocalEntity(id = "u1", orgId = "org1", fullName = "Old Name", email = "ada@example.com", role = "MEMBER", active = true)
        )
        val response = UserResponse(id = "u1", orgId = "org1", fullName = "New Name", email = "ada@example.com", role = "MEMBER", active = true)
        val api = FakeUserApi(onUpdate = { _, _ -> response })
        val repository = UserRepository(api, dao, json)

        val result = repository.updateUser("u1", UpdateUserRequest(fullName = "New Name", role = "MEMBER"))

        assertEquals("New Name", result.fullName)
        assertEquals("New Name", dao.observeById("u1").first()?.fullName)
    }
}
