package com.xechoz.sharefile.server

import fi.iki.elonen.NanoHTTPD
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.ServerSocket

class PortFallbackTest {

    @Test
    fun `randomFreePort returns a bindable port`() {
        val port = ServerSocket(0).use { it.localPort }
        assertTrue(port in 1..65535)
        ServerSocket(port).use { /* bindable */ }
    }

    @Test
    fun `nanohttpd fails to bind an occupied port`() {
        val occupied = ServerSocket(0).use { it.localPort }
        val holder = ServerSocket(occupied)
        try {
            val server = object : NanoHTTPD(occupied) {}
            val failed = try {
                server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
                false
            } catch (e: Exception) {
                true
            } finally {
                server.stop()
            }
            assertTrue("Expected bind to occupied port $occupied to fail", failed)
        } finally {
            holder.close()
        }
    }

    @Test
    fun `fallback candidate differs from occupied port`() {
        val occupied = ServerSocket(0).use { it.localPort }
        val holder = ServerSocket(occupied)
        try {
            val fallback = ServerSocket(0).use { it.localPort }
            assertNotEquals(occupied, fallback)
        } finally {
            holder.close()
        }
    }
}
