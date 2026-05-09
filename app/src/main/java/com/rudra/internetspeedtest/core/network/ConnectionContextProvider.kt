package com.rudra.internetspeedtest.core.network

enum class NetworkType { WIFI, CELLULAR, ETHERNET, VPN, UNKNOWN }
enum class IpType { PUBLIC, CGNAT, PRIVATE }
enum class WifiFrequency(val ghz: Double) {
    FREQ_2_4(2.4),
    FREQ_5(5.0),
    FREQ_6(6.0),
    UNKNOWN(0.0)
}

data class ConnectionContext(
    val networkType: NetworkType,
    val ispName: String,
    val ipType: IpType,
    val signalStrength: Int?,
    val wifiFrequency: WifiFrequency?,
    val carrierName: String = ""
) {
    val displayString: String
        get() = buildString {
            append("Connected to ")
            append(
                when (networkType) {
                    NetworkType.WIFI -> "WiFi"
                    NetworkType.CELLULAR -> carrierName.ifEmpty { "Cellular" }
                    NetworkType.ETHERNET -> "Ethernet"
                    NetworkType.VPN -> "VPN"
                    else -> "Unknown"
                }
            )
            if (ispName.isNotEmpty()) append(" ($ispName)")
            append(" | ")
            append(
                when (ipType) {
                    IpType.PUBLIC -> "Public IP"
                    IpType.CGNAT -> "CGNAT"
                    IpType.PRIVATE -> "Private IP"
                }
            )
            wifiFrequency?.let {
                if (it != WifiFrequency.UNKNOWN) append(" | ${it.ghz}GHz")
            }
        }
}
