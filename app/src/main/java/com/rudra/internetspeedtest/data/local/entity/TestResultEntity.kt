package com.rudra.internetspeedtest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "test_results")
data class TestResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cdnName: String,
    val speedMbps: Double,
    val ttfbMs: Long,
    val downloadTimeMs: Long,
    val timestamp: Long,
    val fileSizeBytes: Long,
    val status: String
)