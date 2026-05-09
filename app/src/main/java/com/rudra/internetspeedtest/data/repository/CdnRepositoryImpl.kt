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
                name = "Cloudflare CDN",
                endpoint = "https://speed.cloudflare.com/__down?bytes=5000000",
                logoRes = R.drawable.ic_cloudflare
            ),
            CdnInfo(
                name = "GitHub Assets",
                endpoint = "https://codeload.github.com/tarballs/test.zip",
                logoRes = R.drawable.ic_github
            ),
            CdnInfo(
                name = "jsDelivr CDN",
                endpoint = "https://cdn.jsdelivr.net/npm/lodash@4.17.21/lodash.min.js",
                logoRes = R.drawable.ic_jsdelivr
            ),
            CdnInfo(
                name = "unpkg CDN",
                endpoint = "https://unpkg.com/react@18/umd/react.production.min.js",
                logoRes = R.drawable.ic_unpkg
            ),
            CdnInfo(
                name = "CDNJS (Cloudflare)",
                endpoint = "https://cdnjs.cloudflare.com/ajax/libs/lodash.js/4.17.21/lodash.min.js",
                logoRes = R.drawable.ic_cdnjs
            ),
            CdnInfo(
                name = "npm Registry",
                endpoint = "https://registry.npmjs.org/react/-/react-18.2.0.tgz",
                logoRes = R.drawable.ic_npm
            ),
            CdnInfo(
                name = "Cloudflare (2)",
                endpoint = "https://cloudflare-releases.com/5mb.bin",
                logoRes = R.drawable.ic_cloudflare
            ),
            CdnInfo(
                name = "jsDelivr (2)",
                endpoint = "https://fastly.jsdelivr.net/gh/lodash/lodash@4.17.21/lodash.min.js",
                logoRes = R.drawable.ic_jsdelivr
            )
        )
    }
}