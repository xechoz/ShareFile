package com.xechoz.sharefile

import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.xechoz.sharefile.platform.DesktopAppContainer
import com.xechoz.sharefile.ui.App
import com.xechoz.sharefile.ui.theme.ShareFileTheme

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "ShareFile",
        state = rememberWindowState(width = 480.dp, height = 800.dp),
    ) {
        val container = remember { DesktopAppContainer.create() }
        ShareFileTheme {
            App(container)
        }
    }
}
