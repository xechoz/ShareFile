package com.xechoz.sharefile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.xechoz.sharefile.AppInfo
import com.xechoz.sharefile.platform.AppContainer
import com.xechoz.sharefile.platform.LocalAppContainer
import com.xechoz.sharefile.ui.layout.ProvideWindowLayout
import com.xechoz.sharefile.ui.receive.ReceiveScreen
import com.xechoz.sharefile.ui.remote.RemoteFilesScreen
import com.xechoz.sharefile.ui.scan.ScanScreen
import com.xechoz.sharefile.ui.share.ShareScreen

sealed interface Screen {
    data object Home : Screen
    data object Share : Screen
    data object Receive : Screen
    data object Scan : Screen
    data class RemoteFiles(val url: String) : Screen
}

@Composable
fun App(container: AppContainer) {
    CompositionLocalProvider(LocalAppContainer provides container) {
        ProvideWindowLayout {
            var screen by remember { mutableStateOf<Screen>(Screen.Home) }

            when (val current = screen) {
                Screen.Home -> HomeScreen(
                    showScan = container.platform.qrScanSupported,
                    showAboutButtons = !container.platform.hasWindowMenu,
                    onShare = { screen = Screen.Share },
                    onReceive = { screen = Screen.Receive },
                    onScan = { screen = Screen.Scan },
                    onAbout = { container.platform.openUrl(AppInfo.AUTHOR_URL) },
                    onFeedback = { container.platform.openUrl(AppInfo.FEEDBACK_URL) },
                )

                Screen.Share -> ShareScreen(
                    onBack = { screen = Screen.Home },
                )

                Screen.Receive -> ReceiveScreen(
                    onBack = { screen = Screen.Home },
                )

                Screen.Scan -> ScanScreen(
                    onBack = { screen = Screen.Home },
                    onResult = { url -> screen = Screen.RemoteFiles(url) },
                )

                is Screen.RemoteFiles -> RemoteFilesScreen(
                    url = current.url,
                    onBack = { screen = Screen.Home },
                )
            }
        }
    }
}
