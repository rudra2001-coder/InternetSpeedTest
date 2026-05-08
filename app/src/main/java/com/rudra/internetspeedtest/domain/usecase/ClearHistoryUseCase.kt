package com.rudra.internetspeedtest.domain.usecase

import com.rudra.internetspeedtest.domain.repository.TestHistoryRepository
import javax.inject.Inject

class ClearHistoryUseCase @Inject constructor(
    private val historyRepository: TestHistoryRepository
) {
    suspend operator fun invoke() {
        historyRepository.clearAllHistory()
    }
}