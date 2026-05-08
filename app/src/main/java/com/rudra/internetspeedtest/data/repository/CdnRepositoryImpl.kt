package com.rudra.internetspeedtest.data.repository

import com.rudra.internetspeedtest.R
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
            ),
            CdnInfo(
                name = "Akamai",
                endpoint = "https://www.akamai.com/speedtest/5mb.dat",
                logoRes = R.drawable.ic_akamai
            ),
            CdnInfo(
                name = "Azure CDN",
                endpoint = "https://azurespeedtest.azureedge.net/5mb",
                logoRes = R.drawable.ic_azure
            ),
            CdnInfo(
                name = "Google Cloud",
                endpoint = "https://storage.googleapis.com/cloud-storage-test-file/5MB.dat",
                logoRes = R.drawable.ic_google_cloud
            ),
            CdnInfo(
                name = "KeyCDN",
                endpoint = "https://keycdn-test.example.com/5mb.bin",
                logoRes = R.drawable.ic_keycdn
            ),
            CdnInfo(
                name = "CDN77",
                endpoint = "https://httpbin.org/stream-bytes/5242880",
                logoRes = R.drawable.ic_cdn77
            ),
            CdnInfo(
                name = "StackPath",
                endpoint = "https://speedtest.stackpath.com/5mb.bin",
                logoRes = R.drawable.ic_stackpath
            )
        )
    }
}