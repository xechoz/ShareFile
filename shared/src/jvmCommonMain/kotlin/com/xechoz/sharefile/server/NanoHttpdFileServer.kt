package com.xechoz.sharefile.server

import com.xechoz.sharefile.model.ReceivedFile
import com.xechoz.sharefile.model.SharedFile
import com.xechoz.sharefile.net.NetworkInfo
import com.xechoz.sharefile.platform.AssetProvider
import com.xechoz.sharefile.platform.ContentSource
import com.xechoz.sharefile.platform.DownloadStore
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.URLEncoder

class NanoHttpdFileServer(
    private val assets: AssetProvider,
    private val downloads: DownloadStore,
    private val content: ContentSource,
    private val port: Int = DEFAULT_PORT,
) : FileServer {

    private var server: NanoHttpdServer? = null

    private val pages = Pages(assets)

    private val _state = MutableStateFlow<ServerState>(ServerState.Stopped)
    override val state: StateFlow<ServerState> = _state.asStateFlow()

    private val _received = MutableStateFlow<List<ReceivedFile>>(emptyList())
    override val received: StateFlow<List<ReceivedFile>> = _received.asStateFlow()

    override fun startShare(files: List<SharedFile>): ServerState =
        start(Mode.Share(files))

    override fun startReceive(): ServerState =
        start(Mode.Receive)

    override fun stop() {
        server?.stop()
        server = null
        _state.value = ServerState.Stopped
    }

    private fun start(mode: Mode): ServerState {
        stop()
        val ip = NetworkInfo.localIpAddress()
            ?: return fail("No local network address")
        val bound = bind(mode) ?: return fail("Failed to start server")
        server = bound
        val path = if (mode is Mode.Share) ShareRoutes.SHARE else ShareRoutes.RECEIVE
        return ServerState.Running("http://$ip:${bound.listeningPort}$path", bound.listeningPort)
            .also { _state.value = it }
    }

    private fun bind(mode: Mode): NanoHttpdServer? {
        for (candidate in ServerPorts.candidates(port)) {
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

    private fun fail(message: String): ServerState =
        ServerState.Error(message).also { _state.value = it }

    private inner class NanoHttpdServer(
        port: Int,
        private val mode: Mode,
    ) : NanoHTTPD(port) {

        override fun serve(session: IHTTPSession): Response = when {
            session.uri.startsWith("/assets/") ->
                serveAsset(session)

            mode is Mode.Share && session.uri.startsWith("/download/") ->
                serveDownload(session)

            mode is Mode.Share && session.uri.startsWith("/thumb/") ->
                serveThumb(session)

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
            val stream = content.openInput(file.locator)
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

        private fun serveThumb(session: IHTTPSession): Response {
            val id = session.uri.removePrefix("/thumb/")
            val file = (mode as Mode.Share).files.firstOrNull { it.id == id }
                ?: return newFixedLengthResponse(
                    Response.Status.NOT_FOUND, MIME_PLAINTEXT, "File not found"
                )
            val stream = content.openInput(file.locator)
                ?: return newFixedLengthResponse(
                    Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Cannot open file"
                )
            val mime = content.mimeType(file.locator).ifBlank { "image/*" }
            return newChunkedResponse(Response.Status.OK, mime, stream)
        }

        private fun serveUpload(session: IHTTPSession): Response = try {
            val saved = MultipartParser.parse(
                input = session.inputStream,
                contentType = session.headers["content-type"].orEmpty(),
                onFile = { name, stream -> downloads.write(name, stream) },
            )
            _received.value = saved + _received.value
            newFixedLengthResponse(
                Response.Status.OK, "text/html; charset=utf-8", pages.successPage(saved)
            )
        } catch (e: Exception) {
            newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT,
                "Upload failed: ${e.message}"
            )
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
            newFixedLengthResponse(Response.Status.OK, "text/html; charset=utf-8", pages.uploadPage())

        private fun serveSharePage(share: Mode.Share): Response =
            newFixedLengthResponse(
                Response.Status.OK, "text/html; charset=utf-8", pages.sharePage(share.files)
            )

        private fun serveAsset(session: IHTTPSession): Response {
            val name = session.uri.removePrefix("/assets/")
            if (name.isEmpty() || name.contains("..")) {
                return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not found")
            }
            val bytes = assets.bytes("web/$name")
                ?: return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not found")
            return newFixedLengthResponse(
                Response.Status.OK, assetMime(name), bytes.inputStream(), bytes.size.toLong()
            )
        }

        private fun assetMime(name: String): String = when {
            name.endsWith(".html") -> "text/html; charset=utf-8"
            name.endsWith(".css") -> "text/css; charset=utf-8"
            name.endsWith(".js") -> "application/javascript; charset=utf-8"
            else -> "application/octet-stream"
        }
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
        const val DEFAULT_PORT = ServerPorts.PRIMARY_PORT
    }
}
