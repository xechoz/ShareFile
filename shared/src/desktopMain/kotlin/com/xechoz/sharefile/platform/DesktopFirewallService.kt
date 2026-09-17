package com.xechoz.sharefile.platform

import com.xechoz.sharefile.net.NetworkInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

class DesktopFirewallService : FirewallService {

    @Volatile
    private var detected: Platform? = null

    override suspend fun check(port: Int): FirewallStatus =
        withContext(Dispatchers.IO) { checkBlocking(port) }

    override suspend fun requestAllow(port: Int): FirewallStatus =
        withContext(Dispatchers.IO) { requestBlocking(port) }

    override fun manualCommand(port: Int): String? {
        val cidr = localCidr()
        return when (platform()) {
            Platform.LINUX_UFW ->
                if (cidr != null) "sudo ufw allow from $cidr to any port $port proto tcp"
                else "sudo ufw allow $port/tcp"

            Platform.LINUX_FIREWALLD ->
                "sudo firewall-cmd --permanent --add-rich-rule='${richRule(port, cidr)}' " +
                    "&& sudo firewall-cmd --reload"

            Platform.WINDOWS ->
                "netsh advfirewall firewall add rule name=${ruleName(port)} dir=in action=allow " +
                    "protocol=TCP localport=$port" + (cidr?.let { " remoteip=$it" } ?: "")

            Platform.MAC, Platform.NONE -> null
        }
    }

    private fun checkBlocking(port: Int): FirewallStatus = when (platform()) {
        Platform.LINUX_UFW -> ufwCheck(port)
        Platform.LINUX_FIREWALLD -> firewalldCheck(port)
        Platform.WINDOWS -> windowsCheck(port)
        Platform.MAC -> macCheck()
        Platform.NONE -> FirewallStatus.Allowed
    }

    private fun requestBlocking(port: Int): FirewallStatus {
        val command = when (platform()) {
            Platform.LINUX_UFW -> ufwAllowArgs(port)
            Platform.LINUX_FIREWALLD -> firewalldAllowArgs(port)
            Platform.WINDOWS -> windowsAllowArgs(port)
            Platform.MAC -> macAllowArgs()
            Platform.NONE -> return FirewallStatus.Allowed
        }
        val result = run(command)
        return if (result.code == 0) checkBlocking(port) else FirewallStatus.Blocked
    }

    private fun platform(): Platform =
        detected ?: detectPlatform().also { detected = it }

    private fun detectPlatform(): Platform {
        val os = System.getProperty("os.name").orEmpty().lowercase()
        return when {
            os.contains("win") -> Platform.WINDOWS
            os.contains("mac") || os.contains("darwin") -> Platform.MAC
            os.contains("linux") -> when {
                ufwEnabled() -> Platform.LINUX_UFW
                firewalldRunning() -> Platform.LINUX_FIREWALLD
                else -> Platform.NONE
            }

            else -> Platform.NONE
        }
    }

    private fun localCidr(): String? = NetworkInfo.localNetwork()?.cidr

    private fun ufwEnabled(): Boolean {
        val conf = runCatching { File("/etc/ufw/ufw.conf").readText() }.getOrNull() ?: return false
        return Regex("""(?im)^\s*ENABLED\s*=\s*yes\s*$""").containsMatchIn(conf)
    }

    private fun firewalldRunning(): Boolean {
        val result = run(listOf("firewall-cmd", "--state"))
        return result.code == 0 && result.stdout.trim() == "running"
    }

    private fun ufwCheck(port: Int): FirewallStatus {
        val rules = runCatching { File("/etc/ufw/user.rules").readText() }.getOrNull()
            ?: return FirewallStatus.Unknown(FirewallUnknown.UfwRulesUnreadable)
        return if (FirewallRules.ufwAllowsPort(rules, port)) FirewallStatus.Allowed
        else FirewallStatus.Blocked
    }

    private fun ufwAllowArgs(port: Int): List<String> {
        val cidr = localCidr()
        return if (cidr != null) {
            listOf("pkexec", "ufw", "allow", "from", cidr, "to", "any", "port", "$port", "proto", "tcp")
        } else {
            listOf("pkexec", "ufw", "allow", "$port/tcp")
        }
    }

    private fun firewalldCheck(port: Int): FirewallStatus {
        val result = run(listOf("firewall-cmd", "--query-rich-rule=${richRule(port, localCidr())}"))
        return if (result.code == 0 && result.stdout.trim().equals("yes", ignoreCase = true)) {
            FirewallStatus.Allowed
        } else {
            FirewallStatus.Blocked
        }
    }

    private fun firewalldAllowArgs(port: Int): List<String> {
        val rule = richRule(port, localCidr())
        val script = "firewall-cmd --permanent --add-rich-rule='$rule' && firewall-cmd --reload"
        return listOf("pkexec", "sh", "-c", script)
    }

    private fun windowsCheck(port: Int): FirewallStatus {
        val state = run(listOf("netsh", "advfirewall", "show", "allprofiles", "state"))
        if (state.code != 0 || !Regex("""State\s+ON""", RegexOption.IGNORE_CASE).containsMatchIn(state.stdout)) {
            return FirewallStatus.Allowed
        }
        val rule = run(listOf("netsh", "advfirewall", "firewall", "show", "rule", "name=${ruleName(port)}"))
        return if (rule.code == 0) FirewallStatus.Allowed else FirewallStatus.Blocked
    }

    private fun windowsAllowArgs(port: Int): List<String> {
        val cidr = localCidr()
        val netsh = buildString {
            append("advfirewall firewall add rule name=${ruleName(port)} dir=in action=allow ")
            append("protocol=TCP localport=$port")
            if (cidr != null) append(" remoteip=$cidr")
        }
        return listOf(
            "powershell", "-NoProfile", "-Command",
            "Start-Process -Verb RunAs -FilePath netsh -ArgumentList '$netsh'",
        )
    }

    private fun macCheck(): FirewallStatus {
        val exe = executablePath() ?: return FirewallStatus.Allowed
        val result = run(listOf("/usr/libexec/ApplicationFirewall/socketfilterfw", "--getappblocked", exe))
        if (result.code != 0) return FirewallStatus.Allowed
        return if (result.stdout.contains("blocked", ignoreCase = true)) FirewallStatus.Blocked
        else FirewallStatus.Allowed
    }

    private fun macAllowArgs(): List<String> {
        val exe = executablePath() ?: return emptyList()
        val script = "/usr/libexec/ApplicationFirewall/socketfilterfw --add '$exe' && " +
            "/usr/libexec/ApplicationFirewall/socketfilterfw --unblockapp '$exe'"
        return listOf("osascript", "-e", "do shell script \"$script\" with administrator privileges")
    }

    private fun executablePath(): String? =
        ProcessHandle.current().info().command().orElse(null)

    private fun richRule(port: Int, cidr: String?): String =
        if (cidr != null) {
            """rule family="ipv4" source address="$cidr" port protocol="tcp" port="$port" accept"""
        } else {
            """rule family="ipv4" port protocol="tcp" port="$port" accept"""
        }

    private fun ruleName(port: Int): String = "ShareFile-$port"

    private fun run(command: List<String>): CommandResult {
        if (command.isEmpty()) return CommandResult(-1, "")
        return try {
            val process = ProcessBuilder(command).redirectErrorStream(true).start()
            val finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            val output = process.inputStream.bufferedReader().use { it.readText() }
            if (!finished) process.destroyForcibly()
            CommandResult(if (finished) process.exitValue() else -1, output)
        } catch (e: Exception) {
            CommandResult(-1, e.message.orEmpty())
        }
    }

    private data class CommandResult(val code: Int, val stdout: String)

    private enum class Platform { LINUX_UFW, LINUX_FIREWALLD, WINDOWS, MAC, NONE }

    private companion object {
        const val TIMEOUT_SECONDS = 120L
    }
}
