package com.rudra.internetspeedtest.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.internetspeedtest.domain.model.TestStatus
import com.rudra.internetspeedtest.domain.usecase.GetCdnsUseCase
import com.rudra.internetspeedtest.domain.usecase.RunBatchTestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getCdnsUseCase: GetCdnsUseCase,
    private val runBatchTestUseCase: RunBatchTestUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadCdns()
    }

    private fun loadCdns() {
        val cdns = getCdnsUseCase()
        _uiState.update { it.copy(availableCdns = cdns) }
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

    fun startTest() {
        val selectedCdns = _uiState.value.selectedCdns
        if (selectedCdns.isEmpty()) return

        val cdnsToTest = _uiState.value.availableCdns
            .filter { selectedCdns.contains(it.name) }
            .map { it.name to it.endpoint }

        viewModelScope.launch {
            _uiState.update { it.copy(isTestRunning = true, testProgress = null) }

            runBatchTestUseCase(
                cdns = cdnsToTest,
                onProgress = { progress ->
                    _uiState.update { it.copy(testProgress = progress) }
                },
                onComplete = { results ->
                    val resultUiList = results
                        .filter { it.status == TestStatus.SUCCESS }
                        .map { result ->
                            TestResultUi(
                                cdnName = result.cdnName,
                                speedMbps = result.speedMbps,
                                ttfbMs = result.ttfbMs
                            )
                        }

                    val maxSpeed = resultUiList.maxOfOrNull { it.speedMbps } ?: 0.0
                    val resultUiWithRank = resultUiList.map { result ->
                        result.copy(isFastest = result.speedMbps == maxSpeed)
                    }

                    _uiState.update {
                        it.copy(
                            isTestRunning = false,
                            testProgress = null,
                            latestResults = resultUiWithRank
                        )
                    }
                }
            )
        }
    }

    fun cancelTest() {
        _uiState.update { it.copy(isTestRunning = false, testProgress = null) }
    }
}