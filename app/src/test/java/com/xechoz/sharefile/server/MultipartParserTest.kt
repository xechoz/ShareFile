package com.xechoz.sharefile.server

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class MultipartParserTest {

    private val boundary = "----ShareFileBoundary"

    private fun body(parts: List<Pair<String, ByteArray>>): ByteArray {
        val out = ByteArrayOutputStream()
        parts.forEach { (name, content) ->
            out.write("--$boundary\r\n".toByteArray())
            out.write(
                "Content-Disposition: form-data; name=\"file\"; filename=\"$name\"\r\n".toByteArray()
            )
            out.write("Content-Type: application/octet-stream\r\n\r\n".toByteArray())
            out.write(content)
            out.write("\r\n".toByteArray())
        }
        out.write("--$boundary--\r\n".toByteArray())
        return out.toByteArray()
    }

    private fun parseAll(payload: ByteArray): Pair<List<Pair<String, ByteArray>>, Int> {
        val saved = mutableListOf<Pair<String, ByteArray>>()
        val input = ByteArrayInputStream(payload)
        val files = MultipartParser.parse(
            input = input,
            contentType = "multipart/form-data; boundary=$boundary",
            onFile = { name, stream ->
                val out = ByteArrayOutputStream()
                stream.copyTo(out)
                saved += name to out.toByteArray()
                "/saved/$name"
            },
        )
        assertEquals(files.size, saved.size)
        files.forEachIndexed { index, file ->
            assertEquals(saved[index].first, file.name)
            assertEquals(saved[index].second.size.toLong(), file.size)
        }
        return saved to input.available()
    }

    @Test
    fun `parses multiple files and consumes the whole body`() {
        val payload = body(
            listOf(
                "a.txt" to "hello".toByteArray(),
                "b.bin" to ByteArray(1000) { (it % 251).toByte() },
                "c.txt" to "world".toByteArray(),
            )
        )
        val (saved, remaining) = parseAll(payload)
        assertEquals(3, saved.size)
        assertArrayEquals("hello".toByteArray(), saved[0].second)
        assertArrayEquals(ByteArray(1000) { (it % 251).toByte() }, saved[1].second)
        assertArrayEquals("world".toByteArray(), saved[2].second)
        assertEquals(0, remaining)
    }

    @Test
    fun `parses file larger than the read buffer`() {
        val content = ByteArray(200_000) { (it % 253).toByte() }
        val (saved, remaining) = parseAll(body(listOf("big.bin" to content)))
        assertEquals(1, saved.size)
        assertArrayEquals(content, saved[0].second)
        assertEquals(0, remaining)
    }

    @Test
    fun `skips parts without a filename`() {
        val out = ByteArrayOutputStream()
        out.write("--$boundary\r\n".toByteArray())
        out.write("Content-Disposition: form-data; name=\"note\"\r\n\r\n".toByteArray())
        out.write("ignored".toByteArray())
        out.write("\r\n--$boundary\r\n".toByteArray())
        out.write(
            "Content-Disposition: form-data; name=\"file\"; filename=\"kept.txt\"\r\n\r\n".toByteArray()
        )
        out.write("kept".toByteArray())
        out.write("\r\n--$boundary--\r\n".toByteArray())

        val (saved, remaining) = parseAll(out.toByteArray())
        assertEquals(1, saved.size)
        assertEquals("kept.txt", saved[0].first)
        assertArrayEquals("kept".toByteArray(), saved[0].second)
        assertEquals(0, remaining)
    }

    @Test
    fun `returns empty list when boundary is missing`() {
        val files = MultipartParser.parse(
            input = ByteArrayInputStream("whatever".toByteArray()),
            contentType = "multipart/form-data",
            onFile = { _, _ -> "" },
        )
        assertTrue(files.isEmpty())
    }
}
