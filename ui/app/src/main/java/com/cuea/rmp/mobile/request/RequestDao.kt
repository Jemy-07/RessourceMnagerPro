package com.cuea.rmp.mobile.request

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RequestDao {

    @Query("SELECT * FROM requests ORDER BY startDate DESC")
    fun observeAll(): Flow<List<RequestLocalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(requests: List<RequestLocalEntity>)

    @Query("DELETE FROM requests")
    suspend fun clearAll()

    // Empty keepIds deletes everything (SQLite: "x NOT IN ()" is true for every row),
    // matching clearAll()'s behavior when there's nothing local-only left to preserve.
    @Query("DELETE FROM requests WHERE id NOT IN (:keepIds)")
    suspend fun deleteAllExcept(keepIds: List<String>)
}

