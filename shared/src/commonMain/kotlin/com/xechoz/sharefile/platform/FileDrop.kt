package com.xechoz.sharefile.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.xechoz.sharefile.model.SharedFile

class FileDropState {
    var isActive by mutableStateOf(false)
        internal set
}

@Composable
fun rememberFileDropState(): FileDropState = remember { FileDropState() }

@Composable
expect fun Modifier.fileDropTarget(
    state: FileDropState,
    onFiles: (List<SharedFile>) -> Unit,
): Modifier
