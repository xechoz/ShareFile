package com.xechoz.sharefile.net

import java.net.Inet4Address
import java.net.NetworkInterface

object NetworkInfo {

    fun localIpAddress(): String? =
        NetworkInterface.getNetworkInterfaces()
            ?.toList()
            ?.filter { it.isUp && !it.isLoopback }
            ?.flatMap { it.inetAddresses.toList() }
            ?.filterIsInstance<Inet4Address>()
            ?.firstOrNull { !it.isLoopbackAddress }
            ?.hostAddress
}
