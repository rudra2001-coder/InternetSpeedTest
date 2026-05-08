package com.rudra.internetspeedtest.domain.repository

import com.rudra.internetspeedtest.domain.model.CdnInfo

interface CdnRepository {
    fun getAvailableCdns(): List<CdnInfo>
}