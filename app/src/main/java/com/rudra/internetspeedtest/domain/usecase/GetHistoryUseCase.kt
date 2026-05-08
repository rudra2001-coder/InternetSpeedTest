package com.rudra.internetspeedtest.domain.usecase

import com.rudra.internetspeedtest.domain.model.TestResult
import com.rudra.internetspeedtest.domain.repository.TestHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHistoryUseCase @Inject constructor(
    private val historyRepository: TestHistoryRepository
) {
    operator fun invoke(): Flow<List<TestResult>> {
        return historyRepository.getAllResults()
    }
}