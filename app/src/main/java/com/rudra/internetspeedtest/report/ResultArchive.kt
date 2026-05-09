package com.rudra.internetspeedtest.report

import android.content.Context
import com.google.gson.GsonBuilder
import com.rudra.internetspeedtest.data.local.dao.TestResultDao
import com.rudra.internetspeedtest.data.local.entity.TestResultEntity
import kotlinx.coroutines.runBlocking
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResultArchive @Inject constructor(
    private val testResultDao: TestResultDao
) {
    fun exportAllData(context: Context): File {
        val results = runBlocking { testResultDao.getAllResultsList() }
        val gson = GsonBuilder().setPrettyPrinting().create()
        val json = gson.toJson(results)

        val dir = File(context.cacheDir, "exports")
        dir.mkdirs()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val csvFile = File(dir, "speedtest_history_$timestamp.csv")
        val jsonFile = File(dir, "speedtest_history_$timestamp.json")

        jsonFile.writeText(json)

        csvFile.bufferedWriter().use { writer ->
            writer.write("id,cdnName,speedMbps,ttfbMs,downloadTimeMs,timestamp,fileSizeBytes,status\n")
            results.forEach { r ->
                writer.write("${r.id},${r.cdnName},${r.speedMbps},${r.ttfbMs},${r.downloadTimeMs},${r.timestamp},${r.fileSizeBytes},${r.status}\n")
            }
        }

        return jsonFile
    }

    suspend fun deleteAllData() {
        testResultDao.clearAll()
    }
}
