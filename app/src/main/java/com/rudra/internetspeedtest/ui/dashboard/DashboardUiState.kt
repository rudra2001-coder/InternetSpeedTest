package com.rudra.internetspeedtest.ui.dashboard

import com.rudra.internetspeedtest.domain.model.CdnInfo
import com.rudra.internetspeedtest.domain.model.CdnTestProgress

data class DashboardUiState(
    val isLoading: Boolean = false,
    val availableCdns: List<CdnInfo> = emptyList(),
    val selectedCdns: Set<String> = emptySet(),
    val testProgress: CdnTestProgress? = null,
    val isTestRunning: Boolean = false,
    val latestResults: List<TestResultUi> = emptyList()
)

data class TestResultUi(
    val cdnName: String,
    val speedMbps: Double,
    val ttfbMs: Long,
    val isFastest: Boolean = false
)