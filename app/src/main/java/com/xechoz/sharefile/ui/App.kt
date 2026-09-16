package com.xechoz.sharefile.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.xechoz.sharefile.ui.receive.ReceiveScreen
import com.xechoz.sharefile.ui.remote.RemoteFilesScreen
import com.xechoz.sharefile.ui.scan.ScanScreen
import com.xechoz.sharefile.ui.share.ShareScreen

private const val AUTHOR_URL = "https://github.com/xechoz"
private const val FEEDBACK_URL = "https://github.com/xechoz/ShareFile/issues"

private fun openUrl(context: Context, url: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

sealed interface Screen {
    data object Home : Screen
    data object Share : Screen
    data object Receive : Screen
    data object Scan : Screen
    data class RemoteFiles(val url: String) : Screen
}

@Composable
fun App() {
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }
    val context = LocalContext.current

    when (val current = screen) {
        Screen.Home -> HomeScreen(
            onShare = { screen = Screen.Share },
            onReceive = { screen = Screen.Receive },
            onScan = { screen = Screen.Scan },
            onAbout = { openUrl(context, AUTHOR_URL) },
            onFeedback = { openUrl(context, FEEDBACK_URL) },
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
