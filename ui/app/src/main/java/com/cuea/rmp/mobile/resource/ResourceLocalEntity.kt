package com.cuea.rmp.mobile.resource

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resources")
data class ResourceLocalEntity(
    @PrimaryKey val id: String,
    val orgId: String,
    val userId: String?,
    val name: String,
    val type: String,
    val hourlyRateAmount: Double,
    val currency: String,
    val availabilityStatus: String,
    val skillsSummary: String
)

