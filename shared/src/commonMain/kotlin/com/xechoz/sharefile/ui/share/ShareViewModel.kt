package com.xechoz.sharefile.ui.share

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xechoz.sharefile.model.SharedFile
import com.xechoz.sharefile.platform.FirewallService
import com.xechoz.sharefile.platform.FirewallStatus
import com.xechoz.sharefile.server.FileServer
import com.xechoz.sharefile.server.ServerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ShareViewModel(
    private val server: FileServer,
    private val firewall: FirewallService,
) : ViewModel() {

    val serverState: StateFlow<ServerState> = server.state

    private val _files = MutableStateFlow<List<SharedFile>>(emptyList())
    val files: StateFlow<List<SharedFile>> = _files.asStateFlow()

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

    fun addFiles(picked: List<SharedFile>) {
        if (picked.isEmpty()) return
        val existing = _files.value.mapTo(mutableSetOf()) { it.locator }
        val added = picked.filterNot { it.locator in existing }
        if (added.isEmpty()) return
        val updated = added + _files.value
        _files.value = updated
        server.startShare(updated)
    }

    fun removeFile(id: String) {
        val updated = _files.value.filterNot { it.id == id }
        _files.value = updated
        if (updated.isEmpty()) server.stop() else server.startShare(updated)
    }

    fun restoreFile(file: SharedFile, index: Int) {
        val current = _files.value
        val updated = current.toMutableList().apply {
            add(index.coerceIn(0, size), file)
        }
        _files.value = updated
        server.startShare(updated)
    }

    fun retry() {
        val current = _files.value
        if (current.isNotEmpty()) server.startShare(current)
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
