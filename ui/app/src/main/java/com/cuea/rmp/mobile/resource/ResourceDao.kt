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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(resources: List<ResourceLocalEntity>)

    @Query("DELETE FROM resources")
    suspend fun clearAll()
}

