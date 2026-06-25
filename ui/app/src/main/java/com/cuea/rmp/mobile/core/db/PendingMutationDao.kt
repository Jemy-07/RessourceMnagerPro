package com.cuea.rmp.mobile.core.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PendingMutationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PendingMutationEntity)

    @Query("SELECT * FROM pending_mutations WHERE status IN (:statuses) ORDER BY createdAt ASC LIMIT :limit")
    suspend fun listByStatus(statuses: List<PendingMutationStatus>, limit: Int = 50): List<PendingMutationEntity>

    @Query("SELECT * FROM pending_mutations WHERE localId = :localId LIMIT 1")
    suspend fun getById(localId: String): PendingMutationEntity?

    @Query("UPDATE pending_mutations SET status = :status, lastError = :lastError WHERE localId = :localId")
    suspend fun updateStatus(localId: String, status: PendingMutationStatus, lastError: String? = null)
}

