package com.xechoz.sharefile.ui.receive

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.xechoz.sharefile.model.ReceivedFile
import com.xechoz.sharefile.server.FileServer
import com.xechoz.sharefile.server.ServerState
import kotlinx.coroutines.flow.StateFlow

class ReceiveViewModel(app: Application) : AndroidViewModel(app) {

    private val server = FileServer(app)

    val serverState: StateFlow<ServerState> = server.state
    val received: StateFlow<List<ReceivedFile>> = server.received

    init {
        server.startReceive()
    }

    override fun onCleared() {
        server.stop()
    }
}
