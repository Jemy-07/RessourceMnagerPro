package com.cuea.rmp.mobile.project

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignmentDao {

    @Query("SELECT * FROM assignments ORDER BY startDate DESC")
    fun observeAll(): Flow<List<AssignmentLocalEntity>>

    // The backend has no "list all assignments" endpoint, only per-project
    // (ProjectApi.getAssignmentsByProject) and per-id (AssignmentApi). These targeted
    // queries let a refresh for one project/resource leave other projects' cached
    // assignments untouched.
    @Query("SELECT * FROM assignments WHERE projectId = :projectId ORDER BY startDate DESC")
    fun observeByProject(projectId: String): Flow<List<AssignmentLocalEntity>>

    @Query("SELECT * FROM assignments WHERE resourceId = :resourceId ORDER BY startDate DESC")
    fun observeByResource(resourceId: String): Flow<List<AssignmentLocalEntity>>

    // Dashboard "upcoming assignments" widget. Note this only sees whatever has already
    // been cached via observeByProject/observeByResource refreshes — there's no backend
    // "list all assignments" endpoint to populate this globally from a cold cache.
    @Query("SELECT * FROM assignments WHERE startDate >= :today ORDER BY startDate ASC LIMIT :limit")
    fun observeUpcoming(today: String, limit: Int): Flow<List<AssignmentLocalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(assignments: List<AssignmentLocalEntity>)

    @Query("DELETE FROM assignments WHERE projectId = :projectId")
    suspend fun clearByProject(projectId: String)

    @Query("DELETE FROM assignments")
    suspend fun clearAll()
}
