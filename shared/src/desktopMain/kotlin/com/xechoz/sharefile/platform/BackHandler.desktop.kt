package com.xechoz.sharefile.platform

import androidx.compose.runtime.Composable

@Composable
actual fun HandleBack(onBack: () -> Unit) {
    // Desktop has no system back gesture; navigation uses the on-screen back button.
}
