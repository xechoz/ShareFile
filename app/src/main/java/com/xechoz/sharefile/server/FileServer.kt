package com.xechoz.sharefile.server

import android.content.Context
import com.xechoz.sharefile.model.ReceivedFile
import com.xechoz.sharefile.model.SharedFile
import com.xechoz.sharefile.storage.DownloadsWriter
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.URLEncoder

class FileServer(
    private val context: Context,
    private val port: Int = DEFAULT_PORT,
) {

    private var server: NanoHttpdServer? = null

    private val _state = MutableStateFlow<ServerState>(ServerState.Stopped)
    val state: StateFlow<ServerState> = _state.asStateFlow()

    private val _received = MutableStateFlow<List<ReceivedFile>>(emptyList())
    val received: StateFlow<List<ReceivedFile>> = _received.asStateFlow()

    fun startShare(files: List<SharedFile>): ServerState =
        start(Mode.Share(files))

    fun startReceive(): ServerState =
        start(Mode.Receive)

    fun stop() {
        server?.stop()
        server = null
        _state.value = ServerState.Stopped
    }

    private fun start(mode: Mode): ServerState {
        stop()
        val ip = com.xechoz.sharefile.net.NetworkInfo.localIpAddress()
            ?: return fail("No local network address")
        val bound = bind(mode) ?: return fail("Failed to start server")
        server = bound
        val path = if (mode is Mode.Share) SHARE_PATH else RECEIVE_PATH
        return ServerState.Running("http://$ip:${bound.listeningPort}$path", bound.listeningPort)
            .also { _state.value = it }
    }

    private fun bind(mode: Mode): NanoHttpdServer? {
        val candidates = listOf(port) + List(FALLBACK_ATTEMPTS) { randomFreePort() }
        for (candidate in candidates) {
            val srv = NanoHttpdServer(candidate, mode)
            try {
                srv.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
                return srv
            } catch (e: Exception) {
                srv.stop()
            }
        }
        return null
    }

    private fun randomFreePort(): Int =
        java.net.ServerSocket(0).use { it.localPort }

    private fun fail(message: String): ServerState =
        ServerState.Error(message).also { _state.value = it }

    private inner class NanoHttpdServer(
        port: Int,
        private val mode: Mode,
    ) : NanoHTTPD(port) {

        override fun serve(session: IHTTPSession): Response = when {
            mode is Mode.Share && session.uri.startsWith("/download/") ->
                serveDownload(session)

            mode is Mode.Share && session.uri == "/api/files" ->
                serveApiFiles()

            mode is Mode.Receive && session.uri == "/upload" && session.method == Method.POST ->
                serveUpload(session)

            mode is Mode.Receive && session.uri == "/files" ->
                serveFileList()

            mode is Mode.Receive -> serveUploadPage()

            else -> serveSharePage(mode as Mode.Share)
        }

        private fun serveDownload(session: IHTTPSession): Response {
            val id = session.uri.removePrefix("/download/")
            val file = (mode as Mode.Share).files.firstOrNull { it.id == id }
                ?: return newFixedLengthResponse(
                    Response.Status.NOT_FOUND, MIME_PLAINTEXT, "File not found"
                )
            val stream = context.contentResolver.openInputStream(file.uri)
                ?: return newFixedLengthResponse(
                    Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Cannot open file"
                )
            val response = newChunkedResponse(
                Response.Status.OK, "application/octet-stream", stream
            )
            response.addHeader(
                "Content-Disposition",
                "attachment; filename=\"${encode(file.name)}\""
            )
            return response
        }

        private fun serveUpload(session: IHTTPSession): Response {
            return try {
                val saved = MultipartParser.parse(
                    input = session.inputStream,
                    contentType = session.headers["content-type"].orEmpty(),
                    onFile = { name, stream -> DownloadsWriter.write(context, name, stream) },
                )
                _received.value = _received.value + saved
                newFixedLengthResponse(
                    Response.Status.OK, "text/html; charset=utf-8", successPage(saved)
                )
            } catch (e: Exception) {
                newFixedLengthResponse(
                    Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT,
                    "Upload failed: ${e.message}"
                )
            }
        }

        private fun serveApiFiles(): Response {
            val json = (mode as Mode.Share).files.joinToString(",", "[", "]") { f ->
                """{"id":"${f.id}","name":"${escapeJson(f.name)}","size":${f.size}}"""
            }
            return newFixedLengthResponse(Response.Status.OK, "application/json", json)
        }

        private fun serveFileList(): Response {
            val json = _received.value.joinToString(",", "[", "]") { f ->
                """{"name":"${escapeJson(f.name)}","size":${f.size}}"""
            }
            return newFixedLengthResponse(Response.Status.OK, "application/json", json)
        }

        private fun serveUploadPage(): Response =
            newFixedLengthResponse(Response.Status.OK, "text/html; charset=utf-8", uploadPage())

        private fun serveSharePage(share: Mode.Share): Response =
            newFixedLengthResponse(
                Response.Status.OK, "text/html; charset=utf-8", sharePage(share.files)
            )
    }

    private fun encode(value: String): String =
        URLEncoder.encode(value, "UTF-8").replace("+", "%20")

    private fun escapeJson(value: String): String =
        value.replace("\\", "\\\\").replace("\"", "\\\"")

    private sealed interface Mode {
        data class Share(val files: List<SharedFile>) : Mode
        data object Receive : Mode
    }

    companion object {
        const val DEFAULT_PORT = 8080
        const val SHARE_PATH = "/share"
        const val RECEIVE_PATH = "/receive"
        private const val FALLBACK_ATTEMPTS = 5
    }
}
