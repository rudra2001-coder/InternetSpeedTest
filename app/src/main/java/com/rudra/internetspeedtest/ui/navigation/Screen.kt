package com.rudra.internetspeedtest.ui.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object History : Screen("history")
    data object Settings : Screen("settings")
    data object Results : Screen("results/{resultsJson}") {
        fun createRoute(resultsJson: String) = "results/$resultsJson"
    }
}