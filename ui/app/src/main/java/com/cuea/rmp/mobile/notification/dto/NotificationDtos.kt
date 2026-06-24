package com.cuea.rmp.mobile.notification.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotificationResponse(
    val id: String,
    val userId: String,
    val type: String,
    val message: String,
    val read: Boolean
)

@Serializable
data class RegisterDeviceTokenRequest(
    val fcmToken: String,
    val platform: String
)

@Serializable
data class DeviceTokenResponse(
    val id: String,
    val userId: String,
    val platform: String
)

