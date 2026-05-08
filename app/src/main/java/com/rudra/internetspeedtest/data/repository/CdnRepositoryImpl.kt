package com.rudra.internetspeedtest.data.repository

import com.rudra.cdnbenchmark.R
import com.rudra.internetspeedtest.domain.model.CdnInfo
import com.rudra.internetspeedtest.domain.repository.CdnRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CdnRepositoryImpl @Inject constructor() : CdnRepository {

    override fun getAvailableCdns(): List<CdnInfo> {
        return listOf(
            CdnInfo(
                name = "Cloudflare",
                endpoint = "https://speed.cloudflare.com/__down?bytes=5000000",
                logoRes = R.drawable.ic_cloudflare
            ),
            CdnInfo(
                name = "Bunny CDN",
                endpoint = "https://speedtest.bunnycdn.com/5mb.bin",
                logoRes = R.drawable.ic_bunny
            ),
            CdnInfo(
                name = "CloudFront",
                endpoint = "https://d1.awsstatic.com/test-assets/5MB.zip",
                logoRes = R.drawable.ic_cloudfront
            ),
            CdnInfo(
                name = "Fastly",
                endpoint = "https://httpbin.org/stream-bytes/5242880",
                logoRes = R.drawable.ic_fastly
            )
        )
    }
}