package com.rudra.internetspeedtest.feature.realitycheck

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealityCheckScreen(
    viewModel: RealityCheckViewModel = hiltViewModel(),
    onExportReport: (RealityCheckEngine.RealityCheckResult) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val testState by viewModel.testState.collectAsState()
    val result by viewModel.result.collectAsState()
    val promisedSpeed by viewModel.promisedSpeed.collectAsState()
    val selectedPlanType by viewModel.selectedPlanType.collectAsState()
    val ispName by viewModel.ispName.collectAsState()
    val isInputValid = viewModel.isInputValid()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reality Check") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = testState) {
            is RealityCheckTestState.Idle -> {
                RealityCheckInputScreen(
                    promisedSpeed = promisedSpeed,
                    selectedPlanType = selectedPlanType,
                    ispName = ispName,
                    isInputValid = isInputValid,
                    onSpeedChange = viewModel::updatePromisedSpeed,
                    onPlanTypeChange = viewModel::updatePlanType,
                    onIspNameChange = viewModel::updateIspName,
                    onStart = viewModel::startRealityCheck,
                    modifier = Modifier.padding(padding)
                )
            }
            
            is RealityCheckTestState.Running -> {
                RealityCheckProgressScreen(
                    currentTest = state.currentTest,
                    totalTests = state.totalTests,
                    phase = state.phase,
                    modifier = Modifier.padding(padding)
                )
            }
            
            is RealityCheckTestState.Complete -> {
                result?.let {
                    RealityCheckResultScreen(
                        result = it,
                        onExport = { onExportReport(it) },
                        onRetest = viewModel::reset,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
            
            is RealityCheckTestState.Error -> {
                ErrorScreen(
                    message = state.message,
                    onRetry = viewModel::reset,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
fun RealityCheckInputScreen(
    promisedSpeed: String,
    selectedPlanType: RealityCheckEngine.PlanType,
    ispName: String,
    isInputValid: Boolean,
    onSpeedChange: (String) -> Unit,
    onPlanTypeChange: (RealityCheckEngine.PlanType) -> Unit,
    onIspNameChange: (String) -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "What speed did your ISP promise you?",
            style = MaterialTheme.typography.headlineSmall
        )
        
        OutlinedTextField(
            value = promisedSpeed,
            onValueChange = onSpeedChange,
            label = { Text("Promised Speed") },
            suffix = { Text("Mbps") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            text = "Plan Type (optional):",
            style = MaterialTheme.typography.titleMedium
        )
        
        Column(modifier = Modifier.selectableGroup()) {
            RealityCheckEngine.PlanType.entries.forEach { planType ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedPlanType == planType,
                            onClick = { onPlanTypeChange(planType) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedPlanType == planType,
                        onClick = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = planType.label,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = planType.typicalExpectation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        OutlinedTextField(
            value = ispName,
            onValueChange = onIspNameChange,
            label = { Text("ISP Name (optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onStart,
            enabled = isInputValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("CHECK MY REALITY")
        }
        
        Text(
            text = "We'll run multiple tests to verify what you actually get.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun RealityCheckProgressScreen(
    currentTest: Int,
    totalTests: Int,
    phase: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(80.dp),
            strokeWidth = 8.dp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Test $currentTest of $totalTests",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = phase,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LinearProgressIndicator(
            progress = { currentTest.toFloat() / totalTests },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun RealityCheckResultScreen(
    result: RealityCheckEngine.RealityCheckResult,
    onExport: () -> Unit,
    onRetest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        RealityVerdictCard(result = result)
        
        RealityEvidenceCard(result = result)
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onRetest,
                modifier = Modifier.weight(1f)
            ) {
                Text("Retest")
            }
            
            Button(
                onClick = onExport,
                modifier = Modifier.weight(1f)
            ) {
                Text("Export Report")
            }
        }
    }
}

@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚠️",
            style = MaterialTheme.typography.displayLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onRetry) {
            Text("Try Again")
        }
    }
}