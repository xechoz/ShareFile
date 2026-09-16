package com.xechoz.sharefile.net

import org.junit.Assert.assertEquals
import org.junit.Test

class FileListParserTest {

    @Test
    fun `parses a single file`() {
        val json = """[{"id":"abc","name":"photo.jpg","size":1234}]"""
        val files = FileListParser.parse(json)
        assertEquals(1, files.size)
        assertEquals("abc", files[0].id)
        assertEquals("photo.jpg", files[0].name)
        assertEquals(1234L, files[0].size)
    }

    @Test
    fun `parses multiple files`() {
        val json = """[{"id":"a","name":"one.txt","size":1},{"id":"b","name":"two.txt","size":2}]"""
        val files = FileListParser.parse(json)
        assertEquals(2, files.size)
        assertEquals("two.txt", files[1].name)
    }

    @Test
    fun `parses empty list`() {
        assertEquals(0, FileListParser.parse("[]").size)
    }

    @Test
    fun `unescapes quotes and backslashes in names`() {
        val json = """[{"id":"a","name":"weird\"name\\x.txt","size":5}]"""
        val files = FileListParser.parse(json)
        assertEquals("weird\"name\\x.txt", files[0].name)
    }

    @Test
    fun `skips malformed entries`() {
        val json = """[{"name":"no-id.txt","size":1},{"id":"b","name":"ok.txt","size":2}]"""
        val files = FileListParser.parse(json)
        assertEquals(1, files.size)
        assertEquals("ok.txt", files[0].name)
    }
}
