package com.xechoz.sharefile.net

import android.content.Context
import com.xechoz.sharefile.model.RemoteFile
import com.xechoz.sharefile.storage.DownloadsWriter
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class RemoteClient(private val context: Context) {

    fun fetchFiles(shareUrl: String): List<RemoteFile> {
        val connection = open("${origin(shareUrl)}/api/files")
        return try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("Server returned ${connection.responseCode}")
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            FileListParser.parse(body)
        } finally {
            connection.disconnect()
        }
    }

    fun download(shareUrl: String, file: RemoteFile): String {
        val connection = open("${origin(shareUrl)}/download/${file.id}")
        return try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("Server returned ${connection.responseCode}")
            }
            DownloadsWriter.write(context, file.name, connection.inputStream)
        } finally {
            connection.disconnect()
        }
    }

    private fun origin(url: String): String = originOf(url)

    private fun open(url: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            requestMethod = "GET"
        }

    companion object {
        private const val TIMEOUT_MS = 10_000

        internal fun originOf(url: String): String {
            val parsed = URL(url)
            val port = if (parsed.port == -1) parsed.defaultPort else parsed.port
            return "${parsed.protocol}://${parsed.host}:$port"
        }
    }
}
