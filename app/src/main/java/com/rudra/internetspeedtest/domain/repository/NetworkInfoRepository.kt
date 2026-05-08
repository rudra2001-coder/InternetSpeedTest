package com.rudra.internetspeedtest.domain.repository

interface NetworkInfoRepository {
    fun getNetworkType(): String
    fun getCarrierName(): String
    fun isConnected(): Boolean
}
