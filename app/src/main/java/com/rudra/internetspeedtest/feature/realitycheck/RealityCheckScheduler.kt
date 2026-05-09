package com.rudra.internetspeedtest.feature.realitycheck

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class RealityCheckScheduler(private val context: Context) {
    
    companion object {
        const val PEAK_HOUR_CHECK_WORK = "reality_check_peak"
        const val OFF_PEAK_CHECK_WORK = "reality_check_offpeak"
        private const val KEY_PROMISED_SPEED = "promised_speed"
        private const val KEY_PLAN_TYPE = "plan_type"
        private const val KEY_ISP_NAME = "isp_name"
    }
    
    fun scheduleAutomatedChecks(
        promisedSpeed: Double,
        planType: RealityCheckEngine.PlanType,
        ispName: String?
    ) {
        schedulePeakHourCheck(promisedSpeed, planType, ispName)
        scheduleOffPeakCheck(promisedSpeed, planType, ispName)
    }
    
    private fun schedulePeakHourCheck(
        promisedSpeed: Double,
        planType: RealityCheckEngine.PlanType,
        ispName: String?
    ) {
        val inputData = Data.Builder()
            .putDouble(KEY_PROMISED_SPEED, promisedSpeed)
            .putString(KEY_PLAN_TYPE, planType.name)
            .putString(KEY_ISP_NAME, ispName)
            .build()
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val peakCheckRequest = PeriodicWorkRequestBuilder<RealityCheckWorker>(
            1, TimeUnit.DAYS
        )
            .setInputData(inputData)
            .setConstraints(constraints)
            .setInitialDelay(calculateInitialDelay(19, 0), TimeUnit.MILLISECONDS) // 7 PM
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PEAK_HOUR_CHECK_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            peakCheckRequest
        )
    }
    
    private fun scheduleOffPeakCheck(
        promisedSpeed: Double,
        planType: RealityCheckEngine.PlanType,
        ispName: String?
    ) {
        val inputData = Data.Builder()
            .putDouble(KEY_PROMISED_SPEED, promisedSpeed)
            .putString(KEY_PLAN_TYPE, planType.name)
            .putString(KEY_ISP_NAME, ispName)
            .build()
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val offPeakCheckRequest = PeriodicWorkRequestBuilder<RealityCheckWorker>(
            1, TimeUnit.DAYS
        )
            .setInputData(inputData)
            .setConstraints(constraints)
            .setInitialDelay(calculateInitialDelay(3, 0), TimeUnit.MILLISECONDS) // 3 AM
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            OFF_PEAK_CHECK_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            offPeakCheckRequest
        )
    }
    
    private fun calculateInitialDelay(targetHour: Int, targetMinute: Int): Long {
        val calendar = java.util.Calendar.getInstance()
        val now = calendar.timeInMillis
        
        calendar.set(java.util.Calendar.HOUR_OF_DAY, targetHour)
        calendar.set(java.util.Calendar.MINUTE, targetMinute)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        
        if (calendar.timeInMillis <= now) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        
        return calendar.timeInMillis - now
    }
    
    fun cancelAllScheduledChecks() {
        WorkManager.getInstance(context).cancelUniqueWork(PEAK_HOUR_CHECK_WORK)
        WorkManager.getInstance(context).cancelUniqueWork(OFF_PEAK_CHECK_WORK)
    }
    
    class RealityCheckWorker(
        context: Context,
        workerParams: WorkerParameters
    ) : CoroutineWorker(context, workerParams) {
        
        override suspend fun doWork(): Result {
            val promisedSpeed = inputData.getDouble(KEY_PROMISED_SPEED, 0.0)
            val planTypeName = inputData.getString(KEY_PLAN_TYPE) ?: "HOME_BROADBAND"
            val ispName = inputData.getString(KEY_ISP_NAME)
            
            if (promisedSpeed <= 0) {
                return Result.failure()
            }
            
            // Note: In a real implementation, we would run the speed test here
            // and save results to the database for history tracking
            
            return Result.success()
        }
    }
}