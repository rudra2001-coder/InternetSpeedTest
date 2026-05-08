package com.rudra.internetspeedtest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.internetspeedtest.data.local.entity.TestResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TestResultDao {
    @Query("SELECT * FROM test_results ORDER BY timestamp DESC")
    fun getAllResults(): Flow<List<TestResultEntity>>

    @Query("SELECT * FROM test_results WHERE cdnName = :cdnName ORDER BY timestamp DESC")
    fun getResultsForCdn(cdnName: String): Flow<List<TestResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: TestResultEntity)

    @Query("DELETE FROM test_results WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM test_results")
    suspend fun clearAll()

    @Query("SELECT * FROM test_results ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatestResults(limit: Int): List<TestResultEntity>

    @Query("SELECT * FROM test_results ORDER BY timestamp DESC")
    suspend fun getAllResultsList(): List<TestResultEntity>
}