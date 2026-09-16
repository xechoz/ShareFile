package com.xechoz.sharefile.server

import com.xechoz.sharefile.model.ReceivedFile
import java.io.BufferedInputStream
import java.io.InputStream

internal object MultipartParser {

    private const val CRLF = "\r\n"
    private const val BUFFER_SIZE = 64 * 1024

    fun parse(
        input: InputStream,
        contentType: String,
        onFile: (name: String, stream: InputStream) -> String,
    ): List<ReceivedFile> {
        val boundary = boundaryOf(contentType) ?: return emptyList()
        val delimiter = "--$boundary"
        val stream = BufferedInputStream(input, BUFFER_SIZE)
        val results = mutableListOf<ReceivedFile>()

        while (true) {
            val line = readLine(stream) ?: break
            if (!line.startsWith(delimiter)) continue
            if (line.endsWith("--")) break

            val headers = readHeaders(stream)
            val filename = filenameOf(headers)
            val part = PartInputStream(stream, "$CRLF$delimiter".toByteArray(Charsets.ISO_8859_1))
            if (filename != null) {
                val savedPath = onFile(filename, part)
                results += ReceivedFile(filename, part.bytesRead, savedPath)
            }
            part.drain()
            if (part.reachedEnd) break
        }
        return results
    }

    private fun boundaryOf(contentType: String): String? =
        Regex("""boundary=(?:"([^"]+)"|([^;]+))""").find(contentType)
            ?.let { it.groupValues[1].ifBlank { it.groupValues[2] }.trim() }

    private fun readHeaders(stream: InputStream): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        while (true) {
            val line = readLine(stream) ?: break
            if (line.isEmpty()) break
            val colon = line.indexOf(':')
            if (colon > 0) {
                headers[line.substring(0, colon).trim().lowercase()] =
                    line.substring(colon + 1).trim()
            }
        }
        return headers
    }

    private fun filenameOf(headers: Map<String, String>): String? {
        val disposition = headers["content-disposition"] ?: return null
        return Regex("""filename="([^"]*)"""").find(disposition)
            ?.groupValues?.getOrNull(1)
            ?.takeIf { it.isNotBlank() }
    }

    private fun readLine(stream: InputStream): String? {
        val buffer = StringBuilder()
        while (true) {
            val b = stream.read()
            if (b == -1) return if (buffer.isEmpty()) null else buffer.toString()
            if (b == '\n'.code) return buffer.toString().removeSuffix("\r")
            buffer.append(b.toChar())
        }
    }

    private class PartInputStream(
        private val source: InputStream,
        private val marker: ByteArray,
    ) : InputStream() {

        var bytesRead: Long = 0
            private set

        var reachedEnd: Boolean = false
            private set

        private val window = IntArray(marker.size)
        private var windowSize = 0
        private var finished = false

        override fun read(): Int {
            if (finished) return -1
            while (true) {
                val b = source.read()
                if (b == -1) {
                    finished = true
                    return if (windowSize > 0) shift() else -1
                }
                if (windowSize < marker.size) {
                    window[windowSize++] = b
                    if (windowSize == marker.size && isMarker()) {
                        finished = true
                        reachedEnd = true
                        return -1
                    }
                    if (windowSize == marker.size) return shift()
                    continue
                }
                return shift()
            }
        }

        private fun shift(): Int {
            val out = window[0]
            System.arraycopy(window, 1, window, 0, windowSize - 1)
            windowSize--
            bytesRead++
            return out
        }

        private fun isMarker(): Boolean {
            for (i in window.indices) {
                if (window[i] != (marker[i].toInt() and 0xFF)) return false
            }
            return true
        }

        fun drain() {
            val buffer = ByteArray(BUFFER_SIZE)
            while (read(buffer) != -1) { /* discard */ }
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            if (len == 0) return 0
            val first = read()
            if (first == -1) return -1
            b[off] = first.toByte()
            var count = 1
            while (count < len) {
                val next = read()
                if (next == -1) break
                b[off + count] = next.toByte()
                count++
            }
            return count
        }
    }
}
