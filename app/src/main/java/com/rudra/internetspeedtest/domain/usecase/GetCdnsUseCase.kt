package com.rudra.internetspeedtest.domain.usecase

import com.rudra.internetspeedtest.domain.model.CdnInfo
import com.rudra.internetspeedtest.domain.repository.CdnRepository
import javax.inject.Inject

class GetCdnsUseCase @Inject constructor(
    private val cdnRepository: CdnRepository
) {
    operator fun invoke(): List<CdnInfo> {
        return cdnRepository.getAvailableCdns()
    }
}