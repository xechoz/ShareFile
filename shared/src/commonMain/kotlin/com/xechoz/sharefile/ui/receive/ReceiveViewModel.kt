package com.xechoz.sharefile.ui.receive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xechoz.sharefile.model.ReceivedFile
import com.xechoz.sharefile.platform.FirewallService
import com.xechoz.sharefile.platform.FirewallStatus
import com.xechoz.sharefile.server.FileServer
import com.xechoz.sharefile.server.ServerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ReceiveViewModel(
    private val server: FileServer,
    private val firewall: FirewallService,
) : ViewModel() {

    val serverState: StateFlow<ServerState> = server.state
    val received: StateFlow<List<ReceivedFile>> = server.received

    private val _firewallStatus = MutableStateFlow<FirewallStatus>(FirewallStatus.Allowed)
    val firewallStatus: StateFlow<FirewallStatus> = _firewallStatus.asStateFlow()

    private val _allowingFirewall = MutableStateFlow(false)
    val allowingFirewall: StateFlow<Boolean> = _allowingFirewall.asStateFlow()

    private val _firewallCommand = MutableStateFlow<String?>(null)
    val firewallCommand: StateFlow<String?> = _firewallCommand.asStateFlow()

    init {
        viewModelScope.launch {
            server.state.collect { state -> refreshFirewall(state) }
        }
        server.startReceive()
    }

    private suspend fun refreshFirewall(state: ServerState) {
        if (state !is ServerState.Running) {
            _firewallStatus.value = FirewallStatus.Allowed
            _firewallCommand.value = null
            return
        }
        _firewallStatus.value = firewall.check(state.port)
        _firewallCommand.value = firewall.manualCommand(state.port)
    }

    fun retry() {
        server.startReceive()
    }

    fun allowFirewall() {
        val port = (server.state.value as? ServerState.Running)?.port ?: return
        if (_allowingFirewall.value) return
        viewModelScope.launch {
            _allowingFirewall.value = true
            _firewallStatus.value = firewall.requestAllow(port)
            _allowingFirewall.value = false
        }
    }

    override fun onCleared() {
        server.stop()
    }
}
