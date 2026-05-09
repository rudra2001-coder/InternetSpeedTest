package com.rudra.internetspeedtest.ui.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object SpeedTest : Screen("speedtest")
    data object History : Screen("history")
    data object Settings : Screen("settings")
    data object More : Screen("more")
    data object Results : Screen("results/{resultsJson}") {
        fun createRoute(resultsJson: String) = "results/$resultsJson"
    }
    data object NetworkNeutrality : Screen("network-neutrality")
    data object RealUseTests : Screen("realuse-tests")
    data object ConnectionHealth : Screen("connection-health")
    data object NetworkInfo : Screen("network-info")
    data object ExportData : Screen("export-data")
    data object About : Screen("about")
}
