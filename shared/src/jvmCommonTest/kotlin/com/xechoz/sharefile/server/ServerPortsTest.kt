package com.xechoz.sharefile.server

import org.junit.Assert.assertEquals
import org.junit.Test

class ServerPortsTest {

    @Test
    fun `candidates start at the primary port and are sequential`() {
        assertEquals(
            listOf(8080, 8081, 8082, 8083, 8084, 8085, 8086, 8087, 8088),
            ServerPorts.candidates(),
        )
    }

    @Test
    fun `candidates honour a custom primary and count`() {
        assertEquals(listOf(9000, 9001, 9002), ServerPorts.candidates(primary = 9000, count = 3))
    }

    @Test
    fun `candidate list is deterministic`() {
        assertEquals(ServerPorts.candidates(), ServerPorts.candidates())
    }
}
