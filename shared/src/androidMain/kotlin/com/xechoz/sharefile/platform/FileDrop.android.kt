package com.xechoz.sharefile.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.xechoz.sharefile.model.SharedFile

@Composable
actual fun Modifier.fileDropTarget(
    state: FileDropState,
    onFiles: (List<SharedFile>) -> Unit,
): Modifier = this
