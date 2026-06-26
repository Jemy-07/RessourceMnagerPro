package com.cuea.rmp.mobile.project

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {

    @Query("SELECT * FROM projects ORDER BY name ASC")
    fun observeAll(): Flow<List<ProjectLocalEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    fun observeById(id: String): Flow<ProjectLocalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(projects: List<ProjectLocalEntity>)

    @Query(
        "UPDATE projects SET syncVersion = :version, serverUpdatedAt = :updatedAt, " +
            "pendingEdit = 0 WHERE id = :id"
    )
    suspend fun applySyncMetadata(id: String, version: Long, updatedAt: String)

    @Query(
        "UPDATE projects SET name = :name, description = :description, startDate = :startDate, " +
            "endDate = :endDate, status = :status, pendingEdit = 1 WHERE id = :id"
    )
    suspend fun applyLocalEdit(
        id: String,
        name: String,
        description: String?,
        startDate: String,
        endDate: String,
        status: String
    )

    @Query("DELETE FROM projects")
    suspend fun clearAll()
}

