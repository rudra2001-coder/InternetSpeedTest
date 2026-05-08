package com.rudra.internetspeedtest.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rudra.internetspeedtest.data.local.dao.TestResultDao
import com.rudra.internetspeedtest.data.local.entity.TestResultEntity

@Database(
    entities = [TestResultEntity::class],
    version = 2,
    exportSchema = false
)
abstract class CdnDatabase : RoomDatabase() {
    abstract fun testResultDao(): TestResultDao

    companion object {
        const val DATABASE_NAME = "cdn_benchmark_db"
    }
}