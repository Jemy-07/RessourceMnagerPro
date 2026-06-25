package com.cuea.rmp.mobile.budget

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets ORDER BY projectId ASC")
    fun observeAll(): Flow<List<BudgetLocalEntity>>

    // One budget per project (BudgetApi is keyed by projectId, not budget id).
    @Query("SELECT * FROM budgets WHERE projectId = :projectId LIMIT 1")
    fun observeByProject(projectId: String): Flow<BudgetLocalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(budgets: List<BudgetLocalEntity>)

    @Query("DELETE FROM budgets")
    suspend fun clearAll()
}
