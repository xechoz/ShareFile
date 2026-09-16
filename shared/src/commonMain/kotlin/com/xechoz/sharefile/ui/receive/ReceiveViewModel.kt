package com.xechoz.sharefile.ui.receive

import androidx.lifecycle.ViewModel
import com.xechoz.sharefile.model.ReceivedFile
import com.xechoz.sharefile.server.FileServer
import com.xechoz.sharefile.server.ServerState
import kotlinx.coroutines.flow.StateFlow

class ReceiveViewModel(
    private val server: FileServer,
) : ViewModel() {

    val serverState: StateFlow<ServerState> = server.state
    val received: StateFlow<List<ReceivedFile>> = server.received

    init {
        server.startReceive()
    }

    fun retry() {
        server.startReceive()
    }

    override fun onCleared() {
        server.stop()
    }
}
