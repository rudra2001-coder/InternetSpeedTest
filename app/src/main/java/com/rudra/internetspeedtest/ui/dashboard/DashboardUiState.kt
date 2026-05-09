package com.rudra.internetspeedtest.ui.dashboard

import com.rudra.internetspeedtest.domain.model.CdnInfo
import com.rudra.internetspeedtest.domain.model.CdnTestProgress

data class DashboardUiState(
    val isLoading: Boolean = false,
    val availableCdns: List<CdnInfo> = emptyList(),
    val selectedCdns: Set<String> = emptySet(),
    val testProgress: CdnTestProgress? = null,
    val isTestRunning: Boolean = false,
    val latestResults: List<TestResultUi> = emptyList(),
    val networkType: String = "Unknown",
    val carrierName: String = "Unknown",
    val isConnected: Boolean = false,
    val isManipulationDetected: Boolean = false,
    val speedVariance: Double = 0.0,
    val medianSpeed: Double = 0.0,
    val avgSpeed: Double = 0.0,
    val latencyMs: Long = 0,
    val bestServer: String = "",
    val worstServer: String = "",
    val testedCount: Int = 0,
    val totalCount: Int = 0,
    val testComplete: Boolean = false
)

data class TestResultUi(
    val cdnName: String,
    val provider: String,
    val speedMbps: Double,
    val ttfbMs: Long,
    val latencyMs: Long,
    val status: TestResultStatus,
    val isFastest: Boolean = false
)

enum class TestResultStatus {
    DONE,
    FAILED,
    IN_PROGRESS
}