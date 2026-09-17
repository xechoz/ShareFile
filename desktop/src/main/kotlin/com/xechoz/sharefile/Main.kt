package com.xechoz.sharefile

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.xechoz.sharefile.platform.DesktopAppContainer
import com.xechoz.sharefile.platform.DesktopUiScale
import com.xechoz.sharefile.platform.DesktopWindowState
import com.xechoz.sharefile.ui.App
import com.xechoz.sharefile.ui.theme.ShareFileTheme

fun main() {
    DesktopUiScale.apply()
    val fontScale = DesktopUiScale.systemFontScale()

    application {
        val (size, position) = remember { DesktopWindowState.load() }
        val windowState = rememberWindowState(size = size, position = position)
        Window(
            onCloseRequest = {
                DesktopWindowState.save(windowState)
                exitApplication()
            },
            title = "ShareFile",
            state = windowState,
        ) {
            LaunchedEffect(Unit) {
                window.minimumSize = DesktopWindowState.minSize
            }
            val container = remember { DesktopAppContainer.create() }
            MenuBar {
                Menu("Help", mnemonic = 'H') {
                    Item("About ShareFile", mnemonic = 'A') {
                        container.platform.openUrl(AppInfo.AUTHOR_URL)
                    }
                    Item("Send Feedback", mnemonic = 'F') {
                        container.platform.openUrl(AppInfo.FEEDBACK_URL)
                    }
                }
            }
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, density.fontScale * fontScale),
            ) {
                ShareFileTheme {
                    App(container)
                }
            }
        }
    }
}
