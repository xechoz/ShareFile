package com.xechoz.sharefile.platform

import com.xechoz.sharefile.model.ReceivedFile

interface PlatformServices {

    val qrScanSupported: Boolean

    val hasWindowMenu: Boolean

    val prefersUrlConnection: Boolean

    val downloadFolderName: String

    fun openUrl(url: String)

    fun openFile(file: ReceivedFile): Boolean

    fun shareFile(file: ReceivedFile)

    fun openDownloadFolder(): Boolean
}

interface DownloadFolderChooser {

    fun chooseDownloadFolder()
}
