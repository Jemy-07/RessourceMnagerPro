package com.cuea.rmp.mobile.resource

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ResourceDao {

    @Query("SELECT * FROM resources ORDER BY name ASC")
    fun observeAll(): Flow<List<ResourceLocalEntity>>

    @Query("SELECT * FROM resources WHERE id = :id")
    fun observeById(id: String): Flow<ResourceLocalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(resources: List<ResourceLocalEntity>)

    @Query(
        "UPDATE resources SET syncVersion = :version, serverUpdatedAt = :updatedAt, " +
            "pendingEdit = 0 WHERE id = :id"
    )
    suspend fun applySyncMetadata(id: String, version: Long, updatedAt: String)

    @Query(
        "UPDATE resources SET name = :name, hourlyRateAmount = :hourlyRateAmount, " +
            "currency = :currency, availabilityStatus = :availabilityStatus, " +
            "pendingEdit = 1 WHERE id = :id"
    )
    suspend fun applyLocalEdit(
        id: String,
        name: String,
        hourlyRateAmount: Double,
        currency: String,
        availabilityStatus: String
    )

    @Query("DELETE FROM resources")
    suspend fun clearAll()
}

