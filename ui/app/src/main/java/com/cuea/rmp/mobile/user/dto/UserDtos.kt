package com.cuea.rmp.mobile.user.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: String,
    val orgId: String,
    val fullName: String,
    val email: String,
    val role: String,
    val active: Boolean
)

@Serializable
data class CreateUserRequest(
    val orgId: String,
    val fullName: String,
    val email: String,
    val password: String,
    val role: String
)

@Serializable
data class UpdateUserRequest(
    val fullName: String,
    val role: String
)

