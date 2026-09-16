package com.xechoz.sharefile.storage

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import java.io.File
import java.io.InputStream

object DownloadsWriter {

    fun write(context: Context, name: String, input: InputStream): String {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, name)
            put(MediaStore.Downloads.MIME_TYPE, mimeTypeFor(name))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
        }
        val uri: Uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: return fallbackWrite(context, name, input)
        resolver.openOutputStream(uri)?.use { out -> input.use { it.copyTo(out) } }
        return uri.toString()
    }

    private fun fallbackWrite(context: Context, name: String, input: InputStream): String {
        val dir = File(context.filesDir, "received").apply { mkdirs() }
        val target = File(dir, name)
        input.use { src -> target.outputStream().use { src.copyTo(it) } }
        return target.absolutePath
    }

    internal fun mimeTypeFor(name: String): String {
        val ext = name.substringAfterLast('.', "").lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
            ?: "application/octet-stream"
    }
}
