package com.xechoz.sharefile.platform

import android.app.Activity
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.xechoz.sharefile.model.ReceivedFile
import com.xechoz.sharefile.storage.ReceivedFiles

class AndroidPlatformServices(
    private val activity: Activity,
) : PlatformServices {

    override val qrScanSupported: Boolean = true

    override val prefersUrlConnection: Boolean = false

    override val downloadFolderName: String = "Downloads"

    override fun openUrl(url: String) {
        activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    override fun openFile(file: ReceivedFile): Boolean = try {
        activity.startActivity(ReceivedFiles.viewIntent(activity, file))
        true
    } catch (e: ActivityNotFoundException) {
        false
    }

    override fun shareFile(file: ReceivedFile) {
        activity.startActivity(
            Intent.createChooser(
                ReceivedFiles.shareIntent(activity, file),
                "Share ${file.name}",
            )
        )
    }

    override fun openDownloadFolder(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        return try {
            activity.startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}
