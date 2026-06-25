package com.cuea.rmp.mobile.notification

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationLocalEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val type: String,
    val message: String,
    val read: Boolean
)

