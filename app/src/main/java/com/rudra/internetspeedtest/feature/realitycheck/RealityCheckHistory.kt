package com.rudra.internetspeedtest.feature.realitycheck

import android.content.Context
import androidx.room.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Entity(tableName = "reality_check_history")
data class RealityCheckHistoryEntity(
    @PrimaryKey
    val testId: String,
    val promisedSpeedMbps: Double,
    val planType: String,
    val ispName: String?,
    val averageSpeedMbps: Double,
    val peakSpeedMbps: Double,
    val minimumSpeedMbps: Double,
    val consistencyPercent: Double,
    val achievedPercentOfPromised: Double,
    val confidenceScore: Int,
    val verdict: String,
    val recommendation: String,
    val timestamp: Long,
    val individualTestResults: String // JSON string of test results
)

@Dao
interface RealityCheckHistoryDao {
    @Query("SELECT * FROM reality_check_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<RealityCheckHistoryEntity>>
    
    @Query("SELECT * FROM reality_check_history WHERE testId = :testId")
    suspend fun getByTestId(testId: String): RealityCheckHistoryEntity?
    
    @Query("SELECT * FROM reality_check_history WHERE ispName = :ispName ORDER BY timestamp DESC")
    fun getByIsp(ispName: String): Flow<List<RealityCheckHistoryEntity>>
    
    @Query("SELECT * FROM reality_check_history WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getByDateRange(startTime: Long): Flow<List<RealityCheckHistoryEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RealityCheckHistoryEntity)
    
    @Delete
    suspend fun delete(entity: RealityCheckHistoryEntity)
    
    @Query("DELETE FROM reality_check_history")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM reality_check_history")
    suspend fun getCount(): Int
}

@Singleton
class RealityCheckHistory @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val database = Room.databaseBuilder(
        context,
        RealityCheckDatabase::class.java,
        "reality_check_database"
    ).build()
    
    private val dao = database.historyDao()
    
    fun getAllHistory(): Flow<List<RealityCheckHistoryEntity>> = dao.getAllHistory()
    
    fun getByIsp(ispName: String): Flow<List<RealityCheckHistoryEntity>> = dao.getByIsp(ispName)
    
    fun getHistoryByDateRange(startTime: Long): Flow<List<RealityCheckHistoryEntity>> = 
        dao.getByDateRange(startTime)
    
    suspend fun saveResult(result: RealityCheckEngine.RealityCheckResult) {
        val entity = RealityCheckHistoryEntity(
            testId = result.testId,
            promisedSpeedMbps = result.config.promisedSpeedMbps,
            planType = result.config.planType.name,
            ispName = result.config.ispName,
            averageSpeedMbps = result.averageSpeedMbps,
            peakSpeedMbps = result.peakSpeedMbps,
            minimumSpeedMbps = result.minimumSpeedMbps,
            consistencyPercent = result.consistencyPercent,
            achievedPercentOfPromised = result.achievedPercentOfPromised,
            confidenceScore = result.confidenceScore,
            verdict = result.verdict.name,
            recommendation = result.recommendation,
            timestamp = result.timestamp,
            individualTestResults = serializeTestResults(result.individualTests)
        )
        dao.insert(entity)
    }
    
    suspend fun deleteResult(testId: String) {
        dao.getByTestId(testId)?.let { dao.delete(it) }
    }
    
    suspend fun clearHistory() {
        dao.deleteAll()
    }
    
    suspend fun getHistoryCount(): Int = dao.getCount()
    
    private fun serializeTestResults(results: List<com.rudra.internetspeedtest.domain.model.SpeedTestResult>): String {
        return results.joinToString(";") { test ->
            "${test.downloadSpeedMbps},${test.uploadSpeedMbps},${test.pingMs}"
        }
    }
    
    fun convertToResult(entity: RealityCheckHistoryEntity): RealityCheckEngine.RealityCheckResult {
        val config = RealityCheckEngine.RealityCheckConfig(
            promisedSpeedMbps = entity.promisedSpeedMbps,
            planType = RealityCheckEngine.PlanType.valueOf(entity.planType),
            ispName = entity.ispName
        )
        
        return RealityCheckEngine.RealityCheckResult(
            config = config,
            individualTests = emptyList(), // Not fully deserialized here
            averageSpeedMbps = entity.averageSpeedMbps,
            peakSpeedMbps = entity.peakSpeedMbps,
            minimumSpeedMbps = entity.minimumSpeedMbps,
            consistencyPercent = entity.consistencyPercent,
            achievedPercentOfPromised = entity.achievedPercentOfPromised,
            confidenceScore = entity.confidenceScore,
            verdict = RealityCheckEngine.RealityVerdict.valueOf(entity.verdict),
            timestamp = entity.timestamp,
            testId = entity.testId,
            recommendation = entity.recommendation
        )
    }
}

@Database(entities = [RealityCheckHistoryEntity::class], version = 1)
abstract class RealityCheckDatabase : RoomDatabase() {
    abstract fun historyDao(): RealityCheckHistoryDao
}