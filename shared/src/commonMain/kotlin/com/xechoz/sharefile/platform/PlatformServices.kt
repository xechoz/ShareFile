package com.xechoz.sharefile.platform

import com.xechoz.sharefile.model.ReceivedFile

interface PlatformServices {

    val qrScanSupported: Boolean

    fun openUrl(url: String)

    fun openFile(file: ReceivedFile): Boolean

    fun shareFile(file: ReceivedFile)
}
