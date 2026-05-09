package com.rudra.internetspeedtest.core.network

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.InetAddress
import java.net.NetworkInterface
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionDetector @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun detect(): ConnectionContext {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return ConnectionContext(
            networkType = NetworkType.UNKNOWN,
            ispName = "",
            ipType = IpType.PRIVATE,
            signalStrength = null,
            wifiFrequency = null
        )
        val capabilities = cm.getNetworkCapabilities(network) ?: return ConnectionContext(
            networkType = NetworkType.UNKNOWN,
            ispName = "",
            ipType = IpType.PRIVATE,
            signalStrength = null,
            wifiFrequency = null
        )

        val networkType = when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> NetworkType.VPN
            else -> NetworkType.UNKNOWN
        }

        val ispName = getIspName()
        val ipType = detectIpType()
        val signalStrength = getSignalStrength(capabilities)
        val wifiFrequency = getWifiFrequency()
        val carrierName = getCarrierName()

        return ConnectionContext(
            networkType = networkType,
            ispName = ispName,
            ipType = ipType,
            signalStrength = signalStrength,
            wifiFrequency = wifiFrequency,
            carrierName = carrierName
        )
    }

    private fun getIspName(): String = try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        for (intf in interfaces) {
            for (addr in intf.inetAddresses) {
                if (!addr.isLoopbackAddress && addr is InetAddress) {
                    val hostName = addr.hostName ?: continue
                    if (hostName.contains(".")) {
                        return hostName.substringAfterLast(".", "Unknown")
                    }
                }
            }
        }
        ""
    } catch (_: Exception) { "" }

    private fun detectIpType(): IpType = try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        for (intf in interfaces) {
            for (addr in intf.inetAddresses) {
                if (!addr.isLoopbackAddress) {
                    val ip = addr.hostAddress ?: continue
                    if (ip.contains(":")) continue
                    val parts = ip.split(".")
                    if (parts.size == 4) {
                        val first = parts[0].toIntOrNull() ?: continue
                        val second = parts.getOrNull(1)?.toIntOrNull()
                        return when {
                            first == 10 || (first == 172 && second != null && second in 16..31)
                                || (first == 192 && parts[1] == "168") -> IpType.PRIVATE
                            first == 100 && second != null && second in 64..127 -> IpType.CGNAT
                            else -> IpType.PUBLIC
                        }
                    }
                }
            }
        }
        IpType.PRIVATE
    } catch (_: Exception) { IpType.PRIVATE }

    private fun getSignalStrength(capabilities: NetworkCapabilities): Int? {
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return capabilities.getSignalStrength()
        }
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return tm.signalStrength?.level?.let { it * 25 }
        }
        return null
    }

    private fun getWifiFrequency(): WifiFrequency? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val info = wm.connectionInfo ?: return null
            val freq = info.frequency
            return when {
                freq in 2400..2500 -> WifiFrequency.FREQ_2_4
                freq in 5000..6000 -> WifiFrequency.FREQ_5
                freq in 6000..7000 -> WifiFrequency.FREQ_6
                else -> WifiFrequency.UNKNOWN
            }
        }
        return null
    }

    private fun getCarrierName(): String = try {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        tm.networkOperatorName ?: ""
    } catch (_: Exception) { "" }
}
