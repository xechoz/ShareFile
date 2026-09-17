package com.xechoz.sharefile.platform

import org.junit.Assert.assertEquals
import org.junit.Test

class DesktopUiScaleTest {

    @Test
    fun `ui scale defaults to one without configuration`() {
        assertEquals(1f, DesktopUiScale.resolve(env = { null }, prop = { null }))
    }

    @Test
    fun `ui scale reads GDK_SCALE`() {
        assertEquals(2f, DesktopUiScale.resolve(env = { key -> if (key == "GDK_SCALE") "2" else null }, prop = { null }))
    }

    @Test
    fun `ui scale property overrides environment`() {
        assertEquals(
            1.5f,
            DesktopUiScale.resolve(
                env = { key -> if (key == DesktopUiScale.UI_SCALE_ENV) "3" else "1" },
                prop = { key -> if (key == DesktopUiScale.UI_SCALE_PROPERTY) "1.5" else null },
            ),
        )
    }

    @Test
    fun `ui scale environment overrides GDK_SCALE`() {
        assertEquals(
            1.25f,
            DesktopUiScale.resolve(
                env = { key -> if (key == DesktopUiScale.UI_SCALE_ENV) "1.25" else "2" },
                prop = { null },
            ),
        )
    }

    @Test
    fun `ui scale clamps invalid and out of range values`() {
        assertEquals(1f, DesktopUiScale.resolve(env = { "not-a-number" }, prop = { null }))
        assertEquals(3f, DesktopUiScale.resolve(env = { "5" }, prop = { null }))
        assertEquals(1f, DesktopUiScale.resolve(env = { "0.25" }, prop = { null }))
    }

    @Test
    fun `gsettings font scale parses quoted and plain doubles`() {
        assertEquals(1f, DesktopUiScale.parseGsettingsFontScale("1.0"))
        assertEquals(1.25f, DesktopUiScale.parseGsettingsFontScale("'1.25'"))
        assertEquals(1f, DesktopUiScale.parseGsettingsFontScale("gsettings: schema not found"))
    }

    @Test
    fun `registry font scale parses hex and decimal percent`() {
        assertEquals(
            1.5f,
            DesktopUiScale.parseRegistryFontScale(
                """
                HKEY_CURRENT_USER\Software\Microsoft\Accessibility
                    TextScaleFactor    REG_DWORD    0x96
                """.trimIndent(),
            ),
        )
        assertEquals(
            1.25f,
            DesktopUiScale.parseRegistryFontScale("    TextScaleFactor    REG_DWORD    125"),
        )
        assertEquals(1f, DesktopUiScale.parseRegistryFontScale("no value here"))
    }

    @Test
    fun `font scale clamps to a sane range`() {
        assertEquals(3f, DesktopUiScale.parseGsettingsFontScale("9.0"))
        assertEquals(0.5f, DesktopUiScale.parseRegistryFontScale("TextScaleFactor REG_DWORD 0x0A"))
    }
}
