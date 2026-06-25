package com.cuea.rmp.mobile.sync

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditLogDao {

    @Query("SELECT * FROM audit_logs ORDER BY occurredAt DESC")
    fun observeAll(): Flow<List<AuditLogLocalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(auditLogs: List<AuditLogLocalEntity>)

    @Query("DELETE FROM audit_logs")
    suspend fun clearAll()
}
