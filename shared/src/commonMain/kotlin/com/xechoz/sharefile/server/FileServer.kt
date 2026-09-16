package com.xechoz.sharefile.server

import com.xechoz.sharefile.model.ReceivedFile
import com.xechoz.sharefile.model.SharedFile
import kotlinx.coroutines.flow.StateFlow

interface FileServer {

    val state: StateFlow<ServerState>

    val received: StateFlow<List<ReceivedFile>>

    fun startShare(files: List<SharedFile>): ServerState

    fun startReceive(): ServerState

    fun stop()
}
