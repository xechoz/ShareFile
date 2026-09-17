package com.xechoz.sharefile.ui.icons

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppIconsTest {

    @Test
    fun `all icons build with path nodes`() {
        val icons = listOf(
            AppIcons.ArrowLeft,
            AppIcons.Close,
            AppIcons.Upload,
            AppIcons.Add,
            AppIcons.OpenInNew,
            AppIcons.Share,
            AppIcons.Download,
            AppIcons.FolderOpen,
            AppIcons.CheckCircle,
            AppIcons.ErrorOutline,
            AppIcons.Warning,
            AppIcons.ContentCopy,
            AppIcons.Check,
            AppIcons.SwapHoriz,
            AppIcons.QrCodeScanner,
        )
        assertEquals(15, icons.size)
        icons.forEach { icon ->
            assertTrue("${icon.name} has no path nodes", icon.root.size > 0)
        }
    }
}
