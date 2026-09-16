package com.xechoz.sharefile.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch

private const val DOUBLE_BACK_WINDOW_MS = 1000L

@Composable
fun DoubleBackHandler(
    message: String,
    onBack: () -> Unit,
    showMessage: suspend (String) -> Unit,
) {
    var lastBackAt by remember { mutableLongStateOf(0L) }
    val scope = rememberCoroutineScope()

    BackHandler {
        val now = System.currentTimeMillis()
        if (now - lastBackAt <= DOUBLE_BACK_WINDOW_MS) {
            onBack()
        } else {
            lastBackAt = now
            scope.launch { showMessage(message) }
        }
    }
}
