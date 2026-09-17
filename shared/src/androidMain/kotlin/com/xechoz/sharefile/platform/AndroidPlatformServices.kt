package com.xechoz.sharefile.platform

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import com.xechoz.sharefile.model.ReceivedFile
import com.xechoz.sharefile.storage.ReceivedFiles

class AndroidPlatformServices(
    private val activity: Activity,
) : PlatformServices {

    override val qrScanSupported: Boolean = true

    override val prefersUrlConnection: Boolean = false

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
}
