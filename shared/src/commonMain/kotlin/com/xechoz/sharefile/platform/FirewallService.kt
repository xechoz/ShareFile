package com.xechoz.sharefile.platform

sealed interface FirewallStatus {

    data object Allowed : FirewallStatus

    data object Blocked : FirewallStatus

    data class Unknown(val reason: FirewallUnknown) : FirewallStatus
}

enum class FirewallUnknown {
    UfwRulesUnreadable,
}

interface FirewallService {

    suspend fun check(port: Int): FirewallStatus

    suspend fun requestAllow(port: Int): FirewallStatus

    fun manualCommand(port: Int): String?
}
