package com.xechoz.sharefile.platform

import com.xechoz.sharefile.model.ReceivedFile
import java.awt.Desktop
import java.io.File
import java.net.URI
import javax.swing.JFileChooser
import javax.swing.SwingUtilities

class DesktopPlatformServices(
    private val downloads: DesktopDownloadStore,
) : PlatformServices, DownloadFolderChooser {

    override val qrScanSupported: Boolean = false

    override val hasWindowMenu: Boolean = true

    override val prefersUrlConnection: Boolean = true

    override val downloadFolderName: String
        get() = downloads.directory.name

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

    override fun openDownloadFolder(): Boolean = runCatching {
        Desktop.getDesktop().open(downloads.directory)
    }.isSuccess

    override fun chooseDownloadFolder() {
        SwingUtilities.invokeLater {
            val chooser = JFileChooser(downloads.directory).apply {
                dialogTitle = "Choose save folder"
                fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            }
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                downloads.setDirectory(chooser.selectedFile)
            }
        }
    }
}
