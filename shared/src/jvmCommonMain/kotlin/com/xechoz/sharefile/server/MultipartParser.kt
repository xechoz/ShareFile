package com.xechoz.sharefile.server

import com.xechoz.sharefile.model.ReceivedFile
import java.io.BufferedInputStream
import java.io.InputStream
import java.io.PushbackInputStream

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
        val marker = "$CRLF$delimiter".toByteArray(Charsets.ISO_8859_1)
        val stream = PushbackInputStream(BufferedInputStream(input, BUFFER_SIZE), BUFFER_SIZE + marker.size)
        val results = mutableListOf<ReceivedFile>()

        if (!seekFirstBoundary(stream, delimiter)) return emptyList()

        while (true) {
            val headers = readHeaders(stream)
            val filename = filenameOf(headers)
            val part = PartInputStream(stream, marker)
            var savedPath: String? = null
            var failure: Exception? = null
            if (filename != null) {
                try {
                    savedPath = onFile(filename, part)
                } catch (e: Exception) {
                    failure = e
                }
            }
            part.drain()
            failure?.let { throw it }
            if (filename != null) {
                results += ReceivedFile(filename, part.bytesRead, savedPath.orEmpty())
            }
            if (!part.reachedEnd) break
            val tail = readLine(stream) ?: break
            if (tail.startsWith("--")) break
        }
        return results
    }

    private fun seekFirstBoundary(stream: InputStream, delimiter: String): Boolean {
        while (true) {
            val line = readLine(stream) ?: return false
            if (line.startsWith(delimiter)) return true
        }
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
        private val source: PushbackInputStream,
        private val marker: ByteArray,
    ) : InputStream() {

        var bytesRead: Long = 0
            private set

        var reachedEnd: Boolean = false
            private set

        private val carry = ByteArray(marker.size - 1)
        private var carrySize = 0
        private var pending: ByteArray? = null
        private var pendingOffset = 0
        private var sourceEnded = false
        private var done = false

        override fun read(): Int {
            val one = ByteArray(1)
            val count = read(one, 0, 1)
            return if (count == -1) -1 else one[0].toInt() and 0xFF
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            if (len == 0) return 0
            if (done) return -1
            while (true) {
                val buffered = pending
                if (buffered != null) {
                    val count = minOf(buffered.size - pendingOffset, len)
                    System.arraycopy(buffered, pendingOffset, b, off, count)
                    pendingOffset += count
                    bytesRead += count
                    if (pendingOffset == buffered.size) {
                        pending = null
                        pendingOffset = 0
                        if (reachedEnd) done = true
                    }
                    return count
                }
                if (sourceEnded) {
                    if (carrySize == 0) {
                        done = true
                        return -1
                    }
                    val count = minOf(carrySize, len)
                    System.arraycopy(carry, 0, b, off, count)
                    System.arraycopy(carry, count, carry, 0, carrySize - count)
                    carrySize -= count
                    bytesRead += count
                    return count
                }
                val chunk = ByteArray(BUFFER_SIZE)
                val read = source.read(chunk)
                if (read == -1) {
                    sourceEnded = true
                    continue
                }
                val data = ByteArray(carrySize + read)
                System.arraycopy(carry, 0, data, 0, carrySize)
                System.arraycopy(chunk, 0, data, carrySize, read)
                val index = indexOf(data, marker)
                if (index >= 0) {
                    reachedEnd = true
                    carrySize = 0
                    source.unread(data, index + marker.size, data.size - index - marker.size)
                    if (index == 0) {
                        done = true
                        return -1
                    }
                    pending = data.copyOf(index)
                    pendingOffset = 0
                    continue
                }
                val keep = minOf(marker.size - 1, data.size)
                val available = data.size - keep
                System.arraycopy(data, data.size - keep, carry, 0, keep)
                carrySize = keep
                if (available == 0) continue
                pending = data.copyOf(available)
                pendingOffset = 0
            }
        }

        private fun indexOf(data: ByteArray, pattern: ByteArray): Int {
            val limit = data.size - pattern.size
            for (start in 0..limit) {
                var matched = true
                for (i in pattern.indices) {
                    if (data[start + i] != pattern[i]) {
                        matched = false
                        break
                    }
                }
                if (matched) return start
            }
            return -1
        }

        fun drain() {
            val buffer = ByteArray(BUFFER_SIZE)
            while (read(buffer) != -1) { /* discard */ }
        }
    }
}
