package com.rudra.internetspeedtest.feature.realitycheck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RealityCheckViewModel @Inject constructor(
    private val engine: RealityCheckEngine
) : ViewModel() {
    
    // Input state
    private val _promisedSpeed = MutableStateFlow("")
    val promisedSpeed: StateFlow<String> = _promisedSpeed
    
    private val _selectedPlanType = MutableStateFlow(RealityCheckEngine.PlanType.HOME_BROADBAND)
    val selectedPlanType: StateFlow<RealityCheckEngine.PlanType> = _selectedPlanType
    
    private val _ispName = MutableStateFlow("")
    val ispName: StateFlow<String> = _ispName
    
    // Test state
    private val _testState = MutableStateFlow<RealityCheckTestState>(RealityCheckTestState.Idle)
    val testState: StateFlow<RealityCheckTestState> = _testState
    
    // Result state
    private val _result = MutableStateFlow<RealityCheckEngine.RealityCheckResult?>(null)
    val result: StateFlow<RealityCheckEngine.RealityCheckResult?> = _result
    
    // Validation
    fun isInputValid(): Boolean {
        return _promisedSpeed.value.toDoubleOrNull()?.let { speed -> speed > 0 } ?: false
    }
    
    fun updatePromisedSpeed(speed: String) {
        // Only allow numbers and one decimal point
        if (speed.matches(Regex("^\\d*\\.?\\d*$"))) {
            _promisedSpeed.value = speed
        }
    }
    
    fun updatePlanType(planType: RealityCheckEngine.PlanType) {
        _selectedPlanType.value = planType
    }
    
    fun updateIspName(name: String) {
        _ispName.value = name
    }
    
    fun startRealityCheck() {
        val speed = _promisedSpeed.value.toDoubleOrNull() ?: return
        
        viewModelScope.launch {
            _testState.value = RealityCheckTestState.Running(
                currentTest = 0,
                totalTests = 3,
                phase = "Preparing test environment..."
            )
            
            try {
                val config = RealityCheckEngine.RealityCheckConfig(
                    promisedSpeedMbps = speed,
                    planType = _selectedPlanType.value,
                    ispName = _ispName.value.ifBlank { null },
                    testCount = 3,
                    testIntervalSeconds = 30
                )
                
                val result = engine.runRealityCheck(config)
                
                _result.value = result
                _testState.value = RealityCheckTestState.Complete
                
            } catch (e: Exception) {
                _testState.value = RealityCheckTestState.Error(
                    "Test failed: ${e.message ?: "Unknown error"}. Please try again."
                )
            }
        }
    }
    
    fun reset() {
        _testState.value = RealityCheckTestState.Idle
        _result.value = null
    }
}

sealed class RealityCheckTestState {
    object Idle : RealityCheckTestState()
    data class Running(
        val currentTest: Int,
        val totalTests: Int,
        val phase: String
    ) : RealityCheckTestState()
    object Complete : RealityCheckTestState()
    data class Error(val message: String) : RealityCheckTestState()
}