package com.xechoz.sharefile.net

import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.Inet4Address
import java.net.InetAddress

class NetworkInfoTest {

    private fun ip(value: String): Inet4Address = InetAddress.getByName(value) as Inet4Address

    @Test
    fun `computes a slash 24 network`() {
        assertEquals("192.168.8.0/24", NetworkInfo.networkCidr(ip("192.168.8.203"), 24))
    }

    @Test
    fun `computes a slash 16 network`() {
        assertEquals("172.20.0.0/16", NetworkInfo.networkCidr(ip("172.20.5.9"), 16))
    }

    @Test
    fun `computes a slash 32 network`() {
        assertEquals("10.0.0.7/32", NetworkInfo.networkCidr(ip("10.0.0.7"), 32))
    }

    @Test
    fun `computes a slash 0 network`() {
        assertEquals("0.0.0.0/0", NetworkInfo.networkCidr(ip("8.8.8.8"), 0))
    }

    @Test
    fun `clamps an out-of-range prefix length`() {
        assertEquals("192.168.8.203/32", NetworkInfo.networkCidr(ip("192.168.8.203"), 99))
    }
}
