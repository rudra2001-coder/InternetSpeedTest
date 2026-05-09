package com.rudra.internetspeedtest.feature.history

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class ScheduleConfig(
    val isEnabled: Boolean,
    val intervalHours: Int = 6,
    val nextTestTime: String = "",
    val testsThisWeek: Int = 0
)

@Singleton
class TestScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val WORK_NAME = "periodic_speed_test"
    }

    fun schedulePeriodicTests(intervalHours: Long = 6) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val work = PeriodicWorkRequestBuilder<SpeedTestWorker>(
            intervalHours, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            work
        )
    }

    fun cancelScheduledTests() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    fun getScheduleConfig(): ScheduleConfig {
        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosForUniqueWork(WORK_NAME).get()
        val isEnabled = workInfos.isNotEmpty()
        return ScheduleConfig(
            isEnabled = isEnabled,
            intervalHours = 6
        )
    }
}

class SpeedTestWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    override fun doWork(): Result {
        return Result.success()
    }
}
