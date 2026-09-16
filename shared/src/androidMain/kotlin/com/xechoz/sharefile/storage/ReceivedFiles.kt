package com.xechoz.sharefile.storage

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.xechoz.sharefile.model.ReceivedFile
import java.io.File

object ReceivedFiles {

    fun viewIntent(context: Context, file: ReceivedFile): Intent {
        val uri = contentUri(context, file)
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType(context, uri, file.name))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newUri(context.contentResolver, file.name, uri)
        }
    }

    fun shareIntent(context: Context, file: ReceivedFile): Intent {
        val uri = contentUri(context, file)
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType(context, uri, file.name)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newUri(context.contentResolver, file.name, uri)
        }
    }

    private fun contentUri(context: Context, file: ReceivedFile): Uri =
        if (file.savedPath.startsWith("content://")) {
            Uri.parse(file.savedPath)
        } else {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                File(file.savedPath),
            )
        }

    private fun mimeType(context: Context, uri: Uri, name: String): String =
        context.contentResolver.getType(uri) ?: DownloadsWriter.mimeTypeFor(name)
}
