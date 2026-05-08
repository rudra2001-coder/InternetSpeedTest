package com.rudra.internetspeedtest.data.repository

import com.rudra.internetspeedtest.data.local.dao.TestResultDao
import com.rudra.internetspeedtest.data.local.entity.TestResultEntity
import com.rudra.internetspeedtest.domain.model.TestResult
import com.rudra.internetspeedtest.domain.model.TestStatus
import com.rudra.internetspeedtest.domain.repository.TestHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestHistoryRepositoryImpl @Inject constructor(
    private val dao: TestResultDao
) : TestHistoryRepository {

    override fun getAllResults(): Flow<List<TestResult>> {
        return dao.getAllResults().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getResultsForCdn(cdnName: String): Flow<List<TestResult>> {
        return dao.getResultsForCdn(cdnName).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertResult(result: TestResult) {
        dao.insert(result.toEntity())
    }

    override suspend fun deleteResult(id: Long) {
        dao.delete(id)
    }

    override suspend fun clearAllHistory() {
        dao.clearAll()
    }

    override suspend fun getLatestResults(limit: Int): List<TestResult> {
        return dao.getLatestResults(limit).map { it.toDomain() }
    }

    override suspend fun exportResults(): String {
        val results = dao.getAllResultsList().map { it.toDomain() }
        return buildString {
            appendLine("CDN,Speed (Mbps),TTFB (ms),Download Time (ms),Timestamp,File Size (bytes),Status")
            results.forEach { result ->
                appendLine("${result.cdnName},${result.speedMbps},${result.ttfbMs},${result.downloadTimeMs},${result.timestamp},${result.fileSizeBytes},${result.status}")
            }
        }
    }

    private fun TestResultEntity.toDomain(): TestResult {
        return TestResult(
            id = id,
            cdnName = cdnName,
            speedMbps = speedMbps,
            ttfbMs = ttfbMs,
            downloadTimeMs = downloadTimeMs,
            timestamp = timestamp,
            fileSizeBytes = fileSizeBytes,
            status = try { TestStatus.valueOf(status) } catch (e: Exception) { TestStatus.FAILED }
        )
    }

    private fun TestResult.toEntity(): TestResultEntity {
        return TestResultEntity(
            id = id,
            cdnName = cdnName,
            speedMbps = speedMbps,
            ttfbMs = ttfbMs,
            downloadTimeMs = downloadTimeMs,
            timestamp = timestamp,
            fileSizeBytes = fileSizeBytes,
            status = status.name
        )
    }
}
