package com.cuea.rmp.mobile.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users ORDER BY fullName ASC")
    fun observeAll(): Flow<List<UserLocalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(users: List<UserLocalEntity>)

    @Query("DELETE FROM users")
    suspend fun clearAll()
}
