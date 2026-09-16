package com.xechoz.sharefile.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import com.xechoz.sharefile.model.SharedFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import java.awt.FileDialog
import java.awt.Frame
import java.util.UUID

@Composable
actual fun rememberFilePicker(onPicked: (List<SharedFile>) -> Unit): () -> Unit {
    val currentOnPicked by rememberUpdatedState(onPicked)
    val scope = rememberCoroutineScope()
    return remember(scope) {
        {
            scope.launch(Dispatchers.Swing) {
                val dialog = FileDialog(null as Frame?, "Select files", FileDialog.LOAD)
                dialog.isMultipleMode = true
                dialog.isVisible = true
                val picked = dialog.files.orEmpty().map { file ->
                    SharedFile(
                        id = UUID.randomUUID().toString(),
                        locator = file.absolutePath,
                        name = file.name,
                        size = file.length(),
                    )
                }
                if (picked.isNotEmpty()) currentOnPicked(picked)
            }
        }
    }
}
