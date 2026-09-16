package com.xechoz.sharefile.platform

import java.io.File
import java.io.InputStream
import java.net.URLConnection

class DesktopAssetProvider : AssetProvider {

    override fun bytes(name: String): ByteArray? =
        javaClass.classLoader?.getResourceAsStream(name)?.use { it.readBytes() }

    override fun text(name: String): String? =
        javaClass.classLoader?.getResourceAsStream(name)?.bufferedReader()?.use { it.readText() }
}

class DesktopDownloadStore : DownloadStore {

    override fun write(name: String, input: InputStream): String {
        val dir = File(System.getProperty("user.home"), "Downloads").apply { mkdirs() }
        val target = uniqueTarget(dir, name)
        input.use { src -> target.outputStream().use { src.copyTo(it) } }
        return target.absolutePath
    }

    private fun uniqueTarget(dir: File, name: String): File {
        val dot = name.lastIndexOf('.')
        val base = if (dot > 0) name.substring(0, dot) else name
        val ext = if (dot > 0) name.substring(dot) else ""
        var candidate = File(dir, name)
        var index = 1
        while (candidate.exists()) {
            candidate = File(dir, "$base ($index)$ext")
            index++
        }
        return candidate
    }
}

class DesktopContentSource : ContentSource {

    override fun openInput(locator: String): InputStream? =
        File(locator).takeIf { it.isFile }?.inputStream()

    override fun mimeType(locator: String): String =
        URLConnection.guessContentTypeFromName(locator) ?: "application/octet-stream"
}
