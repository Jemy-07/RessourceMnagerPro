package com.cuea.rmp.mobile.user

import androidx.room.Entity
import androidx.room.PrimaryKey

// Fields mirror UserResponse (user/dto/UserDtos.kt), the shape returned by UserApi.
@Entity(tableName = "users")
data class UserLocalEntity(
    @PrimaryKey val id: String,
    val orgId: String,
    val fullName: String,
    val email: String,
    val role: String,
    val active: Boolean
)
