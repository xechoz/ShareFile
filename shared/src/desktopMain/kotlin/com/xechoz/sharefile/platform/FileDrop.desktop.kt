package com.xechoz.sharefile.platform

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import com.xechoz.sharefile.model.SharedFile
import java.awt.datatransfer.DataFlavor
import java.io.File
import java.util.UUID

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
actual fun Modifier.fileDropTarget(
    state: FileDropState,
    onFiles: (List<SharedFile>) -> Unit,
): Modifier {
    val target = remember(onFiles) {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {
                state.isActive = true
            }

            override fun onEnded(event: DragAndDropEvent) {
                state.isActive = false
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                state.isActive = false
                val transferable = event.awtTransferable
                if (!transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) return false
                val files = transferable
                    .getTransferData(DataFlavor.javaFileListFlavor) as? List<*> ?: return false
                val dropped = files.filterIsInstance<File>().filter { it.isFile }.map { file ->
                    SharedFile(
                        id = UUID.randomUUID().toString(),
                        locator = file.absolutePath,
                        name = file.name,
                        size = file.length(),
                    )
                }
                if (dropped.isEmpty()) return false
                onFiles(dropped)
                return true
            }
        }
    }
    return dragAndDropTarget(
        shouldStartDragAndDrop = { true },
        target = target,
    )
}
