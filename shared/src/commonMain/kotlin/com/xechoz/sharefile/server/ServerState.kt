package com.xechoz.sharefile.server

sealed interface ServerState {
    data object Stopped : ServerState
    data class Running(val url: String, val port: Int) : ServerState
    data class Error(val reason: ServerError) : ServerState
}

enum class ServerError {
    NoLocalAddress,
    StartFailed,
}
