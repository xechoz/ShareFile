package com.xechoz.sharefile.scan

import org.junit.Assert.assertEquals
import org.junit.Test

class ScannedTargetTest {

    @Test
    fun `share url on any origin is opened in app`() {
        assertEquals(
            ScannedTarget.Share("http://192.168.1.5:8080/share"),
            classifyScanned("http://192.168.1.5:8080/share"),
        )
    }

    @Test
    fun `share url scheme is case insensitive`() {
        assertEquals(
            ScannedTarget.Share("HTTPS://host/share"),
            classifyScanned("HTTPS://host/share"),
        )
    }

    @Test
    fun `share url with query is opened in app`() {
        assertEquals(
            ScannedTarget.Share("http://host:8080/share?token=1"),
            classifyScanned("http://host:8080/share?token=1"),
        )
    }

    @Test
    fun `other http url is opened in browser`() {
        assertEquals(
            ScannedTarget.Browser("https://example.com/path"),
            classifyScanned("https://example.com/path"),
        )
    }

    @Test
    fun `receive url is opened in browser`() {
        assertEquals(
            ScannedTarget.Browser("http://host:8080/receive"),
            classifyScanned("http://host:8080/receive"),
        )
    }

    @Test
    fun `share path must match exactly`() {
        assertEquals(
            ScannedTarget.Browser("http://host/share/"),
            classifyScanned("http://host/share/"),
        )
    }

    @Test
    fun `plain text is unrecognized`() {
        assertEquals(ScannedTarget.Unrecognized, classifyScanned("hello world"))
    }

    @Test
    fun `non http scheme is unrecognized`() {
        assertEquals(ScannedTarget.Unrecognized, classifyScanned("tel:+123"))
    }

    @Test
    fun `blank is unrecognized`() {
        assertEquals(ScannedTarget.Unrecognized, classifyScanned("   "))
    }

    @Test
    fun `surrounding whitespace is trimmed`() {
        assertEquals(
            ScannedTarget.Share("http://host/share"),
            classifyScanned("  http://host/share  "),
        )
    }
}
