package com.xechoz.sharefile.feedback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URLDecoder

class FeedbackUrlTest {

    private fun draft(
        message: String = "Something is broken",
        type: FeedbackType = FeedbackType.Bug,
        email: String? = null,
    ) = FeedbackDraft(
        message = message,
        type = type,
        email = email,
        appName = "Quick File Share",
        appVersion = "1.4.0",
        platform = "Linux 6.10 (amd64)",
    )

    private fun param(url: String, name: String): String {
        val raw = url.substringAfter("?")
            .split("&")
            .first { it.startsWith("$name=") }
            .substringAfter("=")
        return URLDecoder.decode(raw, "UTF-8")
    }

    @Test
    fun `targets the new issue page with the feedback label`() {
        val url = FeedbackUrl.issuesNew(draft())
        assertTrue(url.startsWith("${FeedbackUrl.REPO_URL}/issues/new?"))
        assertTrue(url.contains("labels=feedback"))
    }

    @Test
    fun `encodes the type as a title prefix`() {
        assertEquals("[Suggestion] Something is broken", param(FeedbackUrl.issuesNew(draft(type = FeedbackType.Suggestion)), "title"))
        assertEquals("[Other] Something is broken", param(FeedbackUrl.issuesNew(draft(type = FeedbackType.Other)), "title"))
    }

    @Test
    fun `body carries the message and environment footer`() {
        val body = param(FeedbackUrl.issuesNew(draft()), "body")
        assertTrue(body.startsWith("Something is broken"))
        assertTrue(body.contains("**Type:** Bug"))
        assertTrue(body.contains("**App:** Quick File Share 1.4.0"))
        assertTrue(body.contains("**Platform:** Linux 6.10 (amd64)"))
    }

    @Test
    fun `omits the contact line when email is blank`() {
        assertFalse(param(FeedbackUrl.issuesNew(draft(email = "   ")), "body").contains("Contact"))
        assertFalse(param(FeedbackUrl.issuesNew(draft(email = null)), "body").contains("Contact"))
    }

    @Test
    fun `includes the contact line when email is provided`() {
        assertTrue(param(FeedbackUrl.issuesNew(draft(email = "user@example.com")), "body").contains("**Contact:** user@example.com"))
    }

    @Test
    fun `round trips unicode messages`() {
        val url = FeedbackUrl.issuesNew(draft(message = "无法扫描二维码"))
        assertEquals("[Bug] 无法扫描二维码", param(url, "title"))
        assertTrue(param(url, "body").contains("无法扫描二维码"))
        assertTrue(url.contains("%E6%97%A0%E6%B3%95"))
    }

    @Test
    fun `truncates a long first line in the title`() {
        val title = param(FeedbackUrl.issuesNew(draft(message = "x".repeat(100))), "title")
        assertEquals("[Bug] ${"x".repeat(60)}…", title)
    }

    @Test
    fun `uses only the first line for the title`() {
        val title = param(FeedbackUrl.issuesNew(draft(message = "First line\nSecond line")), "title")
        assertEquals("[Bug] First line", title)
    }
}
