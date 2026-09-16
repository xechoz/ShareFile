package com.xechoz.sharefile.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

@Composable
expect fun rememberFileImage(locator: String): ImageBitmap?
