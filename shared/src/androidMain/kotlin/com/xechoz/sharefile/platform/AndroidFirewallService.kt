package com.xechoz.sharefile.platform

class AndroidFirewallService : FirewallService {

    override suspend fun check(port: Int): FirewallStatus = FirewallStatus.Allowed

    override suspend fun requestAllow(port: Int): FirewallStatus = FirewallStatus.Allowed

    override fun manualCommand(port: Int): String? = null
}
