package com.xechoz.sharefile.net

import org.junit.Assert.assertEquals
import org.junit.Test

class RemoteClientOriginTest {

    @Test
    fun `strips share path and keeps port`() {
        assertEquals(
            "http://192.168.1.10:8080",
            RemoteClient.originOf("http://192.168.1.10:8080/share"),
        )
    }

    @Test
    fun `strips receive path`() {
        assertEquals(
            "http://192.168.1.10:8080",
            RemoteClient.originOf("http://192.168.1.10:8080/receive"),
        )
    }

    @Test
    fun `handles url without path`() {
        assertEquals(
            "http://192.168.1.10:8080",
            RemoteClient.originOf("http://192.168.1.10:8080"),
        )
    }

    @Test
    fun `uses default port when omitted`() {
        assertEquals(
            "http://192.168.1.10:80",
            RemoteClient.originOf("http://192.168.1.10/share"),
        )
    }
}
