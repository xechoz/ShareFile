package com.xechoz.sharefile.platform

import com.xechoz.sharefile.model.ReceivedFile
import com.xechoz.sharefile.resources.Res
import com.xechoz.sharefile.resources.choose_save_folder
import java.awt.Desktop
import java.io.File
import java.net.URI
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString

class DesktopPlatformServices(
    private val downloads: DesktopDownloadStore,
) : PlatformServices, DownloadFolderChooser {

    override val qrScanSupported: Boolean = false

    override val prefersUrlConnection: Boolean = true

    override val downloadFolderName: String
        get() = downloads.directory.name

    override val platformLabel: String =
        "${System.getProperty("os.name")} ${System.getProperty("os.version")} " +
            "(${System.getProperty("os.arch")})"

    override fun openUrl(url: String) {
        runCatching { Desktop.getDesktop().browse(URI(url)) }
    }

    override fun openFile(file: ReceivedFile): Boolean =
        runCatching { Desktop.getDesktop().open(File(file.savedPath)) }.isSuccess

    override fun shareFile(file: ReceivedFile, chooserTitle: String) {
        runCatching {
            val target = File(file.savedPath)
            Desktop.getDesktop().open(target.parentFile ?: target)
        }
    }

    override fun openDownloadFolder(): Boolean = runCatching {
        Desktop.getDesktop().open(downloads.directory)
    }.isSuccess

    override fun chooseDownloadFolder() {
        val title = runBlocking { getString(Res.string.choose_save_folder) }
        SwingUtilities.invokeLater {
            val chooser = JFileChooser(downloads.directory).apply {
                dialogTitle = title
                fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            }
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                downloads.setDirectory(chooser.selectedFile)
            }
        }
    }
}
