package com.xechoz.sharefile.ui.layout

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowLayout { Compact, Expanded }

val LocalWindowLayout = staticCompositionLocalOf { WindowLayout.Compact }

val ExpandedWidthThreshold: Dp = 720.dp

@Composable
fun ProvideWindowLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val layout = if (maxWidth >= ExpandedWidthThreshold) {
            WindowLayout.Expanded
        } else {
            WindowLayout.Compact
        }
        CompositionLocalProvider(LocalWindowLayout provides layout) {
            content()
        }
    }
}
