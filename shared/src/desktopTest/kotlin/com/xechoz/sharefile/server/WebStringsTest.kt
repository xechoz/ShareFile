package com.xechoz.sharefile.server

import com.xechoz.sharefile.platform.DesktopAssetProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WebStringsTest {

    private val token = Regex("""\{\{([^}]+)}}""")
    private val metaTokens = setOf("items", "i18n", "lang", "dir")

    @Test
    fun `templates only reference known keys`() {
        val assets = DesktopAssetProvider()
        listOf("upload.html", "share.html", "success.html").forEach { name ->
            val html = assets.text("web/$name")
            assertNotNull("missing asset $name", html)
            token.findAll(html!!).map { it.groupValues[1] }.forEach { key ->
                if (key !in metaTokens) {
                    WebLocale.entries.forEach { locale ->
                        assertTrue(
                            "$name: ${locale.code} missing key '$key'",
                            key in WebStrings.table(locale),
                        )
                    }
                }
            }
        }
    }

    @Test
    fun `all locales share the same keys`() {
        val reference = WebStrings.table(WebLocale.EN).keys
        WebLocale.entries.forEach { locale ->
            assertEquals(locale.code, reference, WebStrings.table(locale).keys)
        }
    }

    @Test
    fun `accept language picks locale`() {
        assertEquals(WebLocale.ZH, webLocale("zh-CN,zh;q=0.9"))
        assertEquals(WebLocale.ZH, webLocale("zh"))
        assertEquals(WebLocale.ZH_HANT, webLocale("zh-TW"))
        assertEquals(WebLocale.ZH_HANT, webLocale("zh-Hant"))
        assertEquals(WebLocale.EN, webLocale("en-US,en;q=0.9"))
        assertEquals(WebLocale.EN, webLocale(null))
        assertEquals(WebLocale.EN, webLocale("*"))
        assertEquals(WebLocale.RU, webLocale("ru-RU,ru;q=0.9"))
        assertEquals(WebLocale.AR, webLocale("ar"))
        assertEquals(WebLocale.JA, webLocale("ja-JP"))
        assertEquals(WebLocale.PT, webLocale("pt-BR"))
        assertEquals(WebLocale.DE, webLocale("fr;q=0.3,de;q=0.9"))
        assertEquals(WebLocale.JA, webLocale("xx-YY,ja;q=0.8"))
    }

    @Test
    fun `upload page renders zh with no leftover tokens`() {
        val page = Pages(DesktopAssetProvider()).uploadPage(WebStrings.of(WebLocale.ZH))
        assertFalse("unresolved token in page", page.contains("{{"))
        assertTrue(page.contains("""lang="zh""""))
        assertTrue(page.contains("发送文件"))
        assertTrue(page.contains(""""upload.upload":"上传""""))
    }

    @Test
    fun `upload page renders arabic right to left`() {
        val page = Pages(DesktopAssetProvider()).uploadPage(WebStrings.of(WebLocale.AR))
        assertFalse("unresolved token in page", page.contains("{{"))
        assertTrue(page.contains("""lang="ar""""))
        assertTrue(page.contains("""dir="rtl""""))
        assertTrue(page.contains("إرسال الملفات"))
    }
}
