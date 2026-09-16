package com.xechoz.sharefile.net

import java.net.Inet4Address
import java.net.NetworkInterface

data class LocalNetwork(
    val address: String,
    val prefixLength: Int,
    val cidr: String,
    val interfaceName: String,
)

object NetworkInfo {

    fun localIpAddress(): String? = localNetwork()?.address

    fun localNetwork(): LocalNetwork? {
        val interfaces = NetworkInterface.getNetworkInterfaces()?.toList() ?: return null
        for (iface in interfaces) {
            if (!iface.isUp || iface.isLoopback) continue
            for (address in iface.interfaceAddresses) {
                val inet = address.address
                if (inet !is Inet4Address || inet.isLoopbackAddress) continue
                val host = inet.hostAddress ?: continue
                val prefix = address.networkPrefixLength.toInt()
                return LocalNetwork(
                    address = host,
                    prefixLength = prefix,
                    cidr = networkCidr(inet, prefix),
                    interfaceName = iface.name,
                )
            }
        }
        return null
    }

    fun networkCidr(address: Inet4Address, prefixLength: Int): String {
        val prefix = prefixLength.coerceIn(0, 32)
        val ip = address.address.fold(0L) { acc, byte -> (acc shl 8) or (byte.toLong() and 0xFF) }
        val mask = if (prefix == 0) 0L else (0xFFFFFFFFL shl (32 - prefix)) and 0xFFFFFFFFL
        val network = ip and mask
        val a = (network ushr 24) and 0xFF
        val b = (network ushr 16) and 0xFF
        val c = (network ushr 8) and 0xFF
        val d = network and 0xFF
        return "$a.$b.$c.$d/$prefix"
    }
}
