package com.xechoz.sharefile.platform

import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler

@Composable
actual fun HandleBack(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
}
