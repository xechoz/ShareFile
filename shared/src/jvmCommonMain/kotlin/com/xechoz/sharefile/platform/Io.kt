package com.xechoz.sharefile.platform

import java.io.InputStream

interface AssetProvider {
    fun bytes(name: String): ByteArray?
    fun text(name: String): String?
}

interface DownloadStore {
    fun write(name: String, input: InputStream): String
}

interface ContentSource {
    fun openInput(locator: String): InputStream?
    fun mimeType(locator: String): String
}
