package com.xechoz.sharefile.server

sealed interface ServerState {
    data object Stopped : ServerState
    data class Running(val url: String, val port: Int) : ServerState
    data class Error(val message: String) : ServerState
}
