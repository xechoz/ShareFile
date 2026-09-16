package com.xechoz.sharefile.platform

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FirewallRulesTest {

    private val rules = """
        *filter
        -A ufw-user-input -p tcp --dport 22 -j ACCEPT
        -A ufw-user-input -p tcp --dport 8080 -j ACCEPT
        -A ufw-user-input -p tcp --dport 9000:9010 -j ACCEPT
        -A ufw-user-input -p udp --dport 5353 -j ACCEPT
        -A ufw-user-input -p tcp --dport 7000 -j DROP
        COMMIT
    """.trimIndent()

    @Test
    fun `allows an exact tcp port`() {
        assertTrue(FirewallRules.ufwAllowsPort(rules, 8080))
    }

    @Test
    fun `allows a port inside an allowed range`() {
        assertTrue(FirewallRules.ufwAllowsPort(rules, 9005))
    }

    @Test
    fun `rejects a port outside the allowed range`() {
        assertFalse(FirewallRules.ufwAllowsPort(rules, 9011))
    }

    @Test
    fun `rejects a udp-only rule`() {
        assertFalse(FirewallRules.ufwAllowsPort(rules, 5353))
    }

    @Test
    fun `rejects a dropped port`() {
        assertFalse(FirewallRules.ufwAllowsPort(rules, 7000))
    }

    @Test
    fun `rejects an unlisted port`() {
        assertFalse(FirewallRules.ufwAllowsPort(rules, 1234))
    }
}
