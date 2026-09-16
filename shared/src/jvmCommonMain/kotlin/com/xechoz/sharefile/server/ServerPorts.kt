package com.xechoz.sharefile.server

internal object ServerPorts {

    const val PRIMARY_PORT = 8080
    const val PORT_COUNT = 9

    fun candidates(primary: Int = PRIMARY_PORT, count: Int = PORT_COUNT): List<Int> =
        (primary until primary + count).toList()
}
