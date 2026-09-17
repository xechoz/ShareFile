package com.xechoz.sharefile

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.xechoz.sharefile.platform.DesktopAppContainer
import com.xechoz.sharefile.platform.DesktopAssetProvider
import com.xechoz.sharefile.platform.DesktopUiScale
import com.xechoz.sharefile.platform.DesktopWindowState
import com.xechoz.sharefile.ui.App
import com.xechoz.sharefile.ui.theme.ShareFileTheme
import org.jetbrains.skia.Image

fun main() {
    DesktopUiScale.apply()
    val fontScale = DesktopUiScale.systemFontScale()

    application {
        val (size, position) = remember { DesktopWindowState.load() }
        val windowState = rememberWindowState(size = size, position = position)
        val icon = remember {
            DesktopAssetProvider().bytes("sharefile.png")
                ?.let { BitmapPainter(Image.makeFromEncoded(it).toComposeImageBitmap()) }
        }
        Window(
            onCloseRequest = {
                DesktopWindowState.save(windowState)
                exitApplication()
            },
            title = "Quick File Share",
            state = windowState,
            icon = icon,
        ) {
            LaunchedEffect(Unit) {
                window.minimumSize = DesktopWindowState.minSize
            }
            val container = remember { DesktopAppContainer.create() }
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
