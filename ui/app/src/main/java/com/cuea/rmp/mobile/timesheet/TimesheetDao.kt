package com.cuea.rmp.mobile.timesheet

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TimesheetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: TimesheetLocalEntity)

    @Query("SELECT * FROM timesheet_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TimesheetLocalEntity?

    @Query("SELECT * FROM timesheet_entries ORDER BY workDate DESC")
    fun observeAll(): Flow<List<TimesheetLocalEntity>>

    @Query("UPDATE timesheet_entries SET syncState = :syncState, updatedAtMillis = :updatedAtMillis WHERE id = :id")
    suspend fun updateSyncState(id: String, syncState: LocalSyncState, updatedAtMillis: Long)

    @Transaction
    suspend fun markSynced(id: String, updatedAtMillis: Long) {
        updateSyncState(id = id, syncState = LocalSyncState.SYNCED, updatedAtMillis = updatedAtMillis)
    }

    @Transaction
    suspend fun markFailed(id: String, updatedAtMillis: Long) {
        updateSyncState(id = id, syncState = LocalSyncState.FAILED, updatedAtMillis = updatedAtMillis)
    }
}

