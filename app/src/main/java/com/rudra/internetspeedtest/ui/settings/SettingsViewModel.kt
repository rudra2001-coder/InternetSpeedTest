package com.rudra.internetspeedtest.ui.settings

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.internetspeedtest.domain.repository.TestHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val testHistoryRepository: TestHistoryRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    fun exportHistory(onExported: (String) -> Unit) {
        viewModelScope.launch {
            val csvData = testHistoryRepository.exportResults()
            onExported(csvData)
        }
    }

    fun clearHistory(onCleared: () -> Unit) {
        viewModelScope.launch {
            testHistoryRepository.clearAllHistory()
            onCleared()
        }
    }

    fun shareApp() {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Check out CDN Benchmark app for testing CDN speeds!")
            putExtra(Intent.EXTRA_SUBJECT, "CDN Benchmark App")
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share App")
        context.startActivity(shareIntent)
    }
}
