package com.xechoz.sharefile.platform

import android.content.ContentResolver
import android.content.Context
import android.content.res.AssetManager
import android.net.Uri
import com.xechoz.sharefile.storage.DownloadsWriter
import java.io.IOException
import java.io.InputStream

class AndroidAssetProvider(private val assets: AssetManager) : AssetProvider {

    override fun bytes(name: String): ByteArray? = try {
        assets.open(name).use { it.readBytes() }
    } catch (e: IOException) {
        null
    }

    override fun text(name: String): String? = try {
        assets.open(name).bufferedReader().use { it.readText() }
    } catch (e: IOException) {
        null
    }
}

class AndroidDownloadStore(private val context: Context) : DownloadStore {

    override fun write(name: String, input: InputStream): String =
        DownloadsWriter.write(context, name, input)
}

class AndroidContentSource(private val resolver: ContentResolver) : ContentSource {

    override fun openInput(locator: String): InputStream? = try {
        resolver.openInputStream(Uri.parse(locator))
    } catch (e: Exception) {
        null
    }

    override fun mimeType(locator: String): String =
        resolver.getType(Uri.parse(locator)) ?: DownloadsWriter.mimeTypeFor(locator)
}
