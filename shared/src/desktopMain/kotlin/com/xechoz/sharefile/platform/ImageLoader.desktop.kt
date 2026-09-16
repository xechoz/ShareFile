package com.xechoz.sharefile.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image
import java.io.File

@Composable
actual fun rememberFileImage(locator: String): ImageBitmap? {
    val bitmap by produceState<ImageBitmap?>(initialValue = null, locator) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                val bytes = File(locator).takeIf { it.isFile }?.readBytes()
                    ?: return@runCatching null
                Image.makeFromEncoded(bytes).toComposeImageBitmap()
            }.getOrNull()
        }
    }
    return bitmap
}
