package com.xechoz.sharefile.platform

import com.xechoz.sharefile.model.ReceivedFile
import java.awt.Desktop
import java.io.File
import java.net.URI

class DesktopPlatformServices : PlatformServices {

    override val qrScanSupported: Boolean = false

    override val prefersUrlConnection: Boolean = true

    override fun openUrl(url: String) {
        runCatching { Desktop.getDesktop().browse(URI(url)) }
    }

    override fun openFile(file: ReceivedFile): Boolean =
        runCatching { Desktop.getDesktop().open(File(file.savedPath)) }.isSuccess

    override fun shareFile(file: ReceivedFile) {
        runCatching {
            val target = File(file.savedPath)
            Desktop.getDesktop().open(target.parentFile ?: target)
        }
    }
}
