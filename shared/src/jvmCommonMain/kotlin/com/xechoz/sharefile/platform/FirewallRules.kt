package com.xechoz.sharefile.platform

internal object FirewallRules {

    private val DportRegex = Regex("""--dport\s+(\d+)(?::(\d+))?""")

    fun ufwAllowsPort(rules: String, port: Int): Boolean =
        rules.lineSequence().any { line -> ufwLineAllows(line, port) }

    private fun ufwLineAllows(line: String, port: Int): Boolean {
        if (!line.contains("-j ACCEPT")) return false
        if (!line.contains("-p tcp")) return false
        val match = DportRegex.find(line) ?: return false
        val start = match.groupValues[1].toIntOrNull() ?: return false
        val end = match.groupValues[2].toIntOrNull() ?: start
        return port in start..end
    }
}
