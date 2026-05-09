package com.rudra.internetspeedtest.core.testing

sealed class TestPhase(val label: String) {
    object ServerSelection : TestPhase("Selecting optimal servers")
    object PreTestPing : TestPhase("Measuring idle latency")
    data class DownloadProgress(val percent: Int) : TestPhase("Download test")
    data class UploadProgress(val percent: Int) : TestPhase("Upload test")
    object BufferbloatCheck : TestPhase("Measuring loaded latency")
    object Analysis : TestPhase("Analyzing results")
    object Complete : TestPhase("Test complete")
    object Failed : TestPhase("Test failed")
}
