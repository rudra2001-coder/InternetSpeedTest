package com.rudra.internetspeedtest.ui.speedtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.internetspeedtest.data.neutrality.NeutralityCheckEngine
import com.rudra.internetspeedtest.domain.model.NeutralityReport
import com.rudra.internetspeedtest.domain.model.SpeedTestProgress
import com.rudra.internetspeedtest.domain.model.SpeedTestResult
import com.rudra.internetspeedtest.domain.model.TestPhase
import com.rudra.internetspeedtest.domain.model.TestStatus
import com.rudra.internetspeedtest.domain.usecase.RunInternetSpeedTestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SpeedTestUiState(
    val isTestRunning: Boolean = false,
    val progress: SpeedTestProgress = SpeedTestProgress(),
    val result: SpeedTestResult? = null,
    val errorMessage: String? = null,
    val speedHistory: List<Double> = emptyList(),
    val showNeutralityCheck: Boolean = false,
    val neutralityReport: NeutralityReport? = null,
    val isNeutralityRunning: Boolean = false
)

@HiltViewModel
class SpeedTestViewModel @Inject constructor(
    private val runSpeedTestUseCase: RunInternetSpeedTestUseCase,
    private val neutralityEngine: NeutralityCheckEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpeedTestUiState())
    val uiState: StateFlow<SpeedTestUiState> = _uiState

    fun startTest() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isTestRunning = true,
                    result = null,
                    errorMessage = null,
                    progress = SpeedTestProgress(phase = TestPhase.PING),
                    speedHistory = emptyList(),
                    showNeutralityCheck = false,
                    neutralityReport = null
                )
            }

            try {
                val result = runSpeedTestUseCase { progress ->
                    _uiState.update {
                        it.copy(
                            progress = progress,
                            speedHistory = if (progress.currentSpeed > 0)
                                it.speedHistory + progress.currentSpeed
                            else it.speedHistory
                        )
                    }
                }

                _uiState.update {
                    it.copy(
                        isTestRunning = false,
                        result = result,
                        progress = SpeedTestProgress(phase = TestPhase.COMPLETE, status = TestStatus.SUCCESS),
                        showNeutralityCheck = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isTestRunning = false,
                        errorMessage = e.message ?: "Test failed",
                        progress = SpeedTestProgress(phase = TestPhase.FAILED, status = TestStatus.FAILED)
                    )
                }
            }
        }
    }

    fun runNeutralityCheck() {
        viewModelScope.launch {
            val baselineSpeed = _uiState.value.result?.downloadSpeedMbps ?: return@launch

            _uiState.update {
                it.copy(
                    isNeutralityRunning = true,
                    showNeutralityCheck = false
                )
            }

            try {
                val report = neutralityEngine.runNeutralityCheck(baselineSpeed)
                _uiState.update {
                    it.copy(
                        isNeutralityRunning = false,
                        neutralityReport = report
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isNeutralityRunning = false,
                        errorMessage = "Neutrality check failed: ${e.message}"
                    )
                }
            }
        }
    }

    fun resetTest() {
        _uiState.update {
            SpeedTestUiState()
        }
    }
}
