package com.rudra.internetspeedtest.ui.dashboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.internetspeedtest.domain.model.TestStatus
import com.rudra.internetspeedtest.domain.repository.NetworkInfoRepository
import com.rudra.internetspeedtest.domain.usecase.GetCdnsUseCase
import com.rudra.internetspeedtest.domain.usecase.RunBatchTestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getCdnsUseCase: GetCdnsUseCase,
    private val runBatchTestUseCase: RunBatchTestUseCase,
    private val networkInfoRepository: NetworkInfoRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadCdns()
    }

    private fun loadCdns() {
        val cdns = getCdnsUseCase()
        _uiState.update { it.copy(availableCdns = cdns, selectedCdns = cdns.map { c -> c.name }.toSet()) }
    }

    fun startTest() {
        val selectedCdns = _uiState.value.selectedCdns
        if (selectedCdns.isEmpty()) return

        val cdnsToTest = _uiState.value.availableCdns
            .filter { selectedCdns.contains(it.name) }
            .map { it.name to it.endpoint }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isTestRunning = true,
                    testComplete = false,
                    latestResults = emptyList(),
                    testedCount = 0,
                    totalCount = cdnsToTest.size
                )
            }

            runBatchTestUseCase(
                cdns = cdnsToTest,
                onProgress = { progress ->
                    val currentResults = _uiState.value.latestResults.toMutableList()
                    val existingIndex = currentResults.indexOfFirst { it.cdnName == progress.cdnName }
                    val result = TestResultUi(
                        cdnName = progress.cdnName,
                        provider = progress.provider.ifEmpty { "CDN Provider" },
                        speedMbps = progress.currentSpeed,
                        ttfbMs = progress.ttfb,
                        latencyMs = progress.latencyMs,
                        status = TestResultStatus.IN_PROGRESS
                    )
                    if (existingIndex >= 0) {
                        currentResults[existingIndex] = result
                    } else {
                        currentResults.add(result)
                    }
                    _uiState.update {
                        it.copy(
                            testProgress = progress,
                            latestResults = currentResults,
                            testedCount = currentResults.count { r -> r.status != TestResultStatus.IN_PROGRESS }
                        )
                    }
                },
                onComplete = { results ->
                    val resultUiList = results.map { result ->
                        TestResultUi(
                            cdnName = result.cdnName,
                            provider = result.provider.ifEmpty { "CDN Provider" },
                            speedMbps = result.speedMbps,
                            ttfbMs = result.ttfbMs,
                            latencyMs = result.latencyMs,
                            status = if (result.status == TestStatus.SUCCESS) TestResultStatus.DONE else TestResultStatus.FAILED
                        )
                    }

                    val successfulResults = resultUiList.filter { it.speedMbps > 0 }
                    val avgSpeed = if (successfulResults.isNotEmpty()) successfulResults.map { it.speedMbps }.average() else 0.0
                    val sortedBySpeed = successfulResults.sortedBy { it.speedMbps }
                    val medianSpeed = if (sortedBySpeed.isNotEmpty()) {
                        val mid = sortedBySpeed.size / 2
                        if (sortedBySpeed.size % 2 == 0) (sortedBySpeed[mid - 1].speedMbps + sortedBySpeed[mid].speedMbps) / 2
                        else sortedBySpeed[mid].speedMbps
                    } else 0.0
                    val avgLatency = if (successfulResults.isNotEmpty()) successfulResults.map { it.latencyMs }.average().toLong() else 0L
                    val maxSpeed = successfulResults.maxOfOrNull { it.speedMbps } ?: 0.0
                    val minSpeed = successfulResults.minOfOrNull { it.speedMbps } ?: 0.0
                    val speedVariance = if (minSpeed > 0) ((maxSpeed - minSpeed) / minSpeed) * 100 else 0.0

                    val resultUiWithRank = resultUiList.map { result ->
                        result.copy(isFastest = result.speedMbps == maxSpeed && maxSpeed > 0)
                    }

                    val bestServer = successfulResults.maxByOrNull { it.speedMbps }?.cdnName ?: ""
                    val worstServer = successfulResults.minByOrNull { it.speedMbps }?.cdnName ?: ""
                    val isManipulation = speedVariance > 150

                    _uiState.update {
                        it.copy(
                            isTestRunning = false,
                            testProgress = null,
                            latestResults = resultUiWithRank,
                            testComplete = true,
                            testedCount = resultUiList.count { r -> r.status == TestResultStatus.DONE },
                            avgSpeed = avgSpeed,
                            medianSpeed = medianSpeed,
                            latencyMs = avgLatency,
                            bestServer = bestServer,
                            worstServer = worstServer,
                            speedVariance = speedVariance,
                            isManipulationDetected = isManipulation
                        )
                    }
                }
            )
        }
    }

    fun runAgain() {
        _uiState.update {
            it.copy(
                isTestRunning = false,
                testComplete = false,
                latestResults = emptyList(),
                testedCount = 0
            )
        }
        startTest()
    }

    fun copyResults() {
        val results = _uiState.value
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val text = buildString {
            appendLine("CDN Benchmark Results")
            appendLine("====================")
            appendLine()
            appendLine("Download Avg: ${String.format("%.1f", results.avgSpeed)} Mbps")
            appendLine("Median Speed: ${String.format("%.1f", results.medianSpeed)} Mbps")
            appendLine("Latency: ${results.latencyMs}ms")
            appendLine("ISP Manipulation: ${if (results.isManipulationDetected) "Likely" else "Not Detected"}")
            appendLine("Best Server: ${results.bestServer}")
            appendLine("Worst Server: ${results.worstServer}")
            appendLine("Speed Variance: ${results.speedVariance.toInt()}%")
            appendLine()
            appendLine("Endpoint Results:")
            results.latestResults.forEach { result ->
                appendLine("${result.cdnName} - ${result.speedMbps} Mbps, ${result.latencyMs}ms latency, ${result.status.name.lowercase()}")
            }
        }
        val clip = ClipData.newPlainText("CDN Test Results", text)
        clipboardManager.setPrimaryClip(clip)
    }

    fun toggleCdnSelection(cdnName: String) {
        _uiState.update { state ->
            val newSelection = if (state.selectedCdns.contains(cdnName)) {
                state.selectedCdns - cdnName
            } else {
                state.selectedCdns + cdnName
            }
            state.copy(selectedCdns = newSelection)
        }
    }

    fun selectAllCdns() {
        _uiState.update { state ->
            state.copy(selectedCdns = state.availableCdns.map { it.name }.toSet())
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedCdns = emptySet()) }
    }

    fun cancelTest() {
        _uiState.update { it.copy(isTestRunning = false, testProgress = null) }
    }
}