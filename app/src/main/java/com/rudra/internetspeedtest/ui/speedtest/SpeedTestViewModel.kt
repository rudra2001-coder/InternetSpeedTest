package com.rudra.internetspeedtest.ui.speedtest

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.internetspeedtest.core.testing.BufferbloatResult
import com.rudra.internetspeedtest.data.neutrality.NeutralityCheckEngine
import com.rudra.internetspeedtest.domain.model.NeutralityReport
import com.rudra.internetspeedtest.domain.model.SpeedTestProgress
import com.rudra.internetspeedtest.domain.model.SpeedTestResult
import com.rudra.internetspeedtest.domain.model.TestStatus
import com.rudra.internetspeedtest.domain.usecase.RunInternetSpeedTestUseCase
import com.rudra.internetspeedtest.feature.neutrality.CautiousLanguageEngine
import com.rudra.internetspeedtest.feature.neutrality.NeutralityScoreEngine
import com.rudra.internetspeedtest.feature.neutrality.ServiceSpeedTester
import com.rudra.internetspeedtest.feature.neutrality.ServiceVariance
import com.rudra.internetspeedtest.feature.neutrality.ZeroRatingDetector
import com.rudra.internetspeedtest.feature.realuse.CallQualityResult
import com.rudra.internetspeedtest.feature.realuse.DownloadPrediction
import com.rudra.internetspeedtest.feature.realuse.FileDownloadPredictor
import com.rudra.internetspeedtest.feature.realuse.GamingResult
import com.rudra.internetspeedtest.feature.realuse.HouseholdCapacity
import com.rudra.internetspeedtest.feature.realuse.HouseholdSimulator
import com.rudra.internetspeedtest.feature.realuse.NetflixStreamingResult
import com.rudra.internetspeedtest.feature.realuse.NetflixStreamingSimulator
import com.rudra.internetspeedtest.feature.realuse.RealUseReportGenerator
import com.rudra.internetspeedtest.feature.realuse.SocialMediaResult
import com.rudra.internetspeedtest.feature.realuse.SocialMediaSimulator
import com.rudra.internetspeedtest.feature.realuse.StreamingSimResult
import com.rudra.internetspeedtest.feature.realuse.VideoCallSimulator
import com.rudra.internetspeedtest.feature.realuse.YouTubeStreamingSimulator
import com.rudra.internetspeedtest.report.Anomaly
import com.rudra.internetspeedtest.report.AnomalyDetector
import com.rudra.internetspeedtest.report.ConnectionHealthScoreCalculator
import com.rudra.internetspeedtest.report.HealthScore
import com.rudra.internetspeedtest.report.ISPReportCard
import com.rudra.internetspeedtest.report.PeerComparison
import com.rudra.internetspeedtest.report.PeerComparisonResult
import com.rudra.internetspeedtest.report.ResultArchive
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "SpeedTestViewModel"

data class SpeedTestUiState(
    val isTestRunning: Boolean = false,
    val progress: SpeedTestProgress = SpeedTestProgress(),
    val result: SpeedTestResult? = null,
    val errorMessage: String? = null,
    val speedHistory: List<Double> = emptyList(),
    val showNeutralityCheck: Boolean = false,
    val neutralityReport: NeutralityReport? = null,
    val isNeutralityRunning: Boolean = false,
    val showRealUseScreen: Boolean = false,
    val isRealUseRunning: Boolean = false,
    val realUseSummary: String = "",
    val streamingResult: StreamingSimResult? = null,
    val netflixResult: NetflixStreamingResult? = null,
    val callQualityResult: CallQualityResult? = null,
    val gamingResult: GamingResult? = null,
    val socialMediaResult: SocialMediaResult? = null,
    val downloadPredictions: List<DownloadPrediction>? = null,
    val householdCapacity: HouseholdCapacity? = null,
    val healthScore: HealthScore? = null,
    val anomalies: List<Anomaly> = emptyList(),
    val peerComparisonResult: PeerComparisonResult? = null,
    val ispReportCard: ISPReportCard? = null,
    val showMethodology: Boolean = false,
    val showAuditLog: Boolean = false,
    val threadData: Map<Int, List<Pair<Long, Double>>> = emptyMap()
)

@HiltViewModel
class SpeedTestViewModel @Inject constructor(
    private val runSpeedTestUseCase: RunInternetSpeedTestUseCase,
    private val neutralityEngine: NeutralityCheckEngine,
    private val serviceSpeedTester: ServiceSpeedTester,
    private val neutralityScoreEngine: NeutralityScoreEngine,
    private val cautiousLanguageEngine: CautiousLanguageEngine,
    private val zeroRatingDetector: ZeroRatingDetector,
    private val youtubeSimulator: YouTubeStreamingSimulator,
    private val netflixSimulator: NetflixStreamingSimulator,
    private val videoCallSimulator: VideoCallSimulator,
    private val gamingSimulator: com.rudra.internetspeedtest.feature.realuse.GamingLatencySimulator,
    private val socialMediaSimulator: SocialMediaSimulator,
    private val fileDownloadPredictor: FileDownloadPredictor,
    private val householdSimulator: HouseholdSimulator,
    private val healthCalculator: ConnectionHealthScoreCalculator,
    private val anomalyDetector: AnomalyDetector,
    private val peerComparison: PeerComparison,
    private val resultArchive: ResultArchive
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
                    progress = SpeedTestProgress(phase = com.rudra.internetspeedtest.core.testing.TestPhase.ServerSelection),
                    speedHistory = emptyList(),
                    showNeutralityCheck = false,
                    neutralityReport = null,
                    streamingResult = null,
                    callQualityResult = null,
                    gamingResult = null,
                    socialMediaResult = null,
                    healthScore = null,
                    anomalies = emptyList()
                )
            }

            try {
                val result = runSpeedTestUseCase { progress ->
                    _uiState.update {
                        it.copy(
                            progress = progress,
                            speedHistory = if (progress.currentSpeed > 0)
                                it.speedHistory + progress.currentSpeed
                            else it.speedHistory,
                            threadData = updateThreadData(it.threadData, progress)
                        )
                    }
                }

                val health = safeCall("calculateHealthScore") { calculateHealthScore(result) }
                val anomalies = safeCall("detectAnomalies") {
                    anomalyDetector.detectAnomalies(result, resultArchive.let { emptyList() })
                } ?: emptyList()

                _uiState.update {
                    it.copy(
                        isTestRunning = false,
                        result = result,
                        progress = SpeedTestProgress(
                            phase = com.rudra.internetspeedtest.core.testing.TestPhase.Complete,
                            status = TestStatus.SUCCESS
                        ),
                        showNeutralityCheck = true,
                        healthScore = health,
                        anomalies = anomalies
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Speed test failed", e)
                _uiState.update {
                    it.copy(
                        isTestRunning = false,
                        errorMessage = e.message ?: "Test failed",
                        progress = SpeedTestProgress(
                            phase = com.rudra.internetspeedtest.core.testing.TestPhase.Failed,
                            status = TestStatus.FAILED
                        )
                    )
                }
            }
        }
    }

    fun runNeutralityCheck() {
        viewModelScope.launch {
            val baselineSpeed = _uiState.value.result?.downloadSpeedMbps ?: return@launch

            _uiState.update {
                it.copy(isNeutralityRunning = true, showNeutralityCheck = false)
            }

            try {
                val report = safeCall("neutralityCheck") {
                    neutralityEngine.runNeutralityCheck(baselineSpeed)
                } ?: return@launch

                val variances = safeCall("buildVariances") {
                    report.serviceResults.map { sr ->
                        ServiceVariance(
                            service = sr.serviceName,
                            baselineSpeed = baselineSpeed,
                            serviceSpeed = sr.speedMbps,
                            deviationPercent = sr.deviationPercent,
                            severity = when {
                                kotlin.math.abs(sr.deviationPercent) < 10 -> ServiceVariance.Severity.MINIMAL
                                kotlin.math.abs(sr.deviationPercent) < 25 -> ServiceVariance.Severity.MODERATE
                                kotlin.math.abs(sr.deviationPercent) < 50 -> ServiceVariance.Severity.SIGNIFICANT
                                else -> ServiceVariance.Severity.SEVERE
                            }
                        )
                    }
                } ?: emptyList()

                val neutralityScore = safeCall("neutralityScore") {
                    neutralityScoreEngine.calculate(variances)
                }
                val cautiousReport = safeCall("cautiousLanguage") {
                    neutralityScore?.let { cautiousLanguageEngine.generateReport(it, variances) }
                } ?: "Network consistency analysis completed."

                _uiState.update {
                    it.copy(
                        isNeutralityRunning = false,
                        neutralityReport = report.copy(summary = cautiousReport)
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Neutrality check failed", e)
                _uiState.update {
                    it.copy(
                        isNeutralityRunning = false,
                        neutralityReport = NeutralityReport(
                            baselineSpeedMbps = baselineSpeed,
                            neutralityScore = 50,
                            variationDetected = false,
                            summary = "Neutrality check encountered an error. Results may be incomplete.",
                            recommendation = "Try running the test again."
                        )
                    )
                }
            }
        }
    }

    fun runRealUseTests() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRealUseRunning = true, showRealUseScreen = true) }

            val speedResult = _uiState.value.result ?: run {
                _uiState.update { it.copy(isRealUseRunning = false) }
                return@launch
            }

            val streamingResult = safeCall("youtubeSimulator") { youtubeSimulator.simulate(20) }
            val netflixResult = safeCall("netflixSimulator") { netflixSimulator.simulate() }
            val callQuality = safeCall("videoCallSimulator") { videoCallSimulator.simulate(15) }
            val gamingResult = safeCall("gamingSimulator") { gamingSimulator.simulate(20) }
            val socialResult = safeCall("socialMediaSimulator") { socialMediaSimulator.simulate() }

            val predictions = safeCall("downloadPredictor") {
                listOf(
                    fileDownloadPredictor.predict(10.0, speedResult),
                    fileDownloadPredictor.predict(100.0, speedResult),
                    fileDownloadPredictor.predict(1024.0, speedResult)
                )
            }

            val household = safeCall("householdSimulator") { householdSimulator.calculate(speedResult) }

            val reportGen = RealUseReportGenerator()
            val report = safeCall("reportGenerator") {
                reportGen.generate(
                    streamingResult = streamingResult,
                    callQuality = callQuality,
                    gamingResult = gamingResult,
                    socialMediaResult = socialResult,
                    householdCapacity = household
                )
            }

            _uiState.update {
                it.copy(
                    isRealUseRunning = false,
                    streamingResult = streamingResult,
                    netflixResult = netflixResult,
                    callQualityResult = callQuality,
                    gamingResult = gamingResult,
                    socialMediaResult = socialResult,
                    downloadPredictions = predictions,
                    householdCapacity = household,
                    realUseSummary = report?.summary() ?: "Some tests could not complete."
                )
            }
        }
    }

    fun runPeerComparison(ispName: String, planSpeed: String, region: String) {
        viewModelScope.launch {
            val result = _uiState.value.result ?: return@launch
            val comparison = safeCall("peerComparison") {
                peerComparison.compare(result, com.rudra.internetspeedtest.report.PeerGroup(ispName, planSpeed, region))
            }
            if (comparison != null) {
                _uiState.update { it.copy(peerComparisonResult = comparison) }
            }
        }
    }

    fun toggleMethodology() {
        _uiState.update { it.copy(showMethodology = !it.showMethodology) }
    }

    fun toggleAuditLog() {
        _uiState.update { it.copy(showAuditLog = !it.showAuditLog) }
    }

    fun resetTest() {
        _uiState.update { SpeedTestUiState() }
    }

    private suspend fun calculateHealthScore(result: SpeedTestResult): HealthScore? {
        val bufferbloatResult = safeCall("bufferbloatCalculation") {
            result.bufferbloatResult ?: BufferbloatResult.calculate(
                result.pingMs, result.loadedDownloadPingMs, result.loadedUploadPingMs
            )
        } ?: return null

        val neutralityScore = com.rudra.internetspeedtest.feature.neutrality.NeutralityScore(76, 18, "Mixed variation")
        return safeCall("healthCalculator") {
            healthCalculator.calculate(result, bufferbloatResult, neutralityScore, null)
        }
    }

    private suspend fun <T> safeCall(name: String, block: suspend () -> T): T? {
        return try {
            block()
        } catch (e: Exception) {
            Log.e(TAG, "Safe call '$name' failed", e)
            null
        }
    }

    private fun updateThreadData(
        existing: Map<Int, List<Pair<Long, Double>>>,
        progress: SpeedTestProgress
    ): Map<Int, List<Pair<Long, Double>>> {
        if (progress.currentSpeed <= 0) return existing
        val now = System.currentTimeMillis()
        val updated = existing.toMutableMap()
        val threadId = (existing.size % 4).coerceAtMost(3)
        val list = updated.getOrPut(threadId) { emptyList() }
        updated[threadId] = list + (now to progress.currentSpeed)
        return updated
    }
}
