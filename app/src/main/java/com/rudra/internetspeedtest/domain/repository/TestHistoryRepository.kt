package com.rudra.internetspeedtest.domain.repository

import com.rudra.internetspeedtest.domain.model.TestResult
import kotlinx.coroutines.flow.Flow

interface TestHistoryRepository {
    fun getAllResults(): Flow<List<TestResult>>
    fun getResultsForCdn(cdnName: String): Flow<List<TestResult>>
    suspend fun insertResult(result: TestResult)
    suspend fun deleteResult(id: Long)
    suspend fun clearAllHistory()
    suspend fun getLatestResults(limit: Int): List<TestResult>
}