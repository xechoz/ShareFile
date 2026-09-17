package com.xechoz.sharefile.net

import com.xechoz.sharefile.model.RemoteFile
import com.xechoz.sharefile.platform.DownloadStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URI

class HttpFileDownloader(
    private val downloads: DownloadStore,
) : FileDownloader {

    override suspend fun fetchFiles(shareUrl: String): List<RemoteFile> =
        withContext(Dispatchers.IO) {
            val connection = open("${originOf(shareUrl)}/api/files")
            try {
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw RemoteException(connection.responseCode)
                }
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                FileListParser.parse(body)
            } finally {
                connection.disconnect()
            }
        }

    override suspend fun download(shareUrl: String, file: RemoteFile): String =
        withContext(Dispatchers.IO) {
            val connection = open("${originOf(shareUrl)}/download/${file.id}")
            try {
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw RemoteException(connection.responseCode)
                }
                downloads.write(file.name, connection.inputStream)
            } finally {
                connection.disconnect()
            }
        }

    private fun open(url: String): HttpURLConnection =
        (URI(url).toURL().openConnection() as HttpURLConnection).apply {
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            requestMethod = "GET"
        }

    companion object {
        private const val TIMEOUT_MS = 10_000

        internal fun originOf(url: String): String {
            val parsed = URI(url).toURL()
            val port = if (parsed.port == -1) parsed.defaultPort else parsed.port
            return "${parsed.protocol}://${parsed.host}:$port"
        }
    }
}
