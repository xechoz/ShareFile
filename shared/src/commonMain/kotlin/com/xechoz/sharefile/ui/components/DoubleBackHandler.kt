package com.xechoz.sharefile.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.xechoz.sharefile.platform.HandleBack
import kotlinx.coroutines.launch
import kotlin.time.TimeMark
import kotlin.time.TimeSource

private const val DOUBLE_BACK_WINDOW_MS = 1000L

@Composable
fun DoubleBackHandler(
    message: String,
    onBack: () -> Unit,
    showMessage: suspend (String) -> Unit,
) {
    val timeSource = remember { TimeSource.Monotonic }
    var lastBack by remember { mutableStateOf<TimeMark?>(null) }
    val scope = rememberCoroutineScope()

    HandleBack {
        val now = timeSource.markNow()
        val elapsed = lastBack?.elapsedNow()?.inWholeMilliseconds
        if (elapsed != null && elapsed <= DOUBLE_BACK_WINDOW_MS) {
            onBack()
        } else {
            lastBack = now
            scope.launch { showMessage(message) }
        }
    }
}
