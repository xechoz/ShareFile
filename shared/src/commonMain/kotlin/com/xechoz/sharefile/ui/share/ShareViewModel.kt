package com.xechoz.sharefile.ui.share

import androidx.lifecycle.ViewModel
import com.xechoz.sharefile.model.SharedFile
import com.xechoz.sharefile.server.FileServer
import com.xechoz.sharefile.server.ServerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ShareViewModel(
    private val server: FileServer,
) : ViewModel() {

    val serverState: StateFlow<ServerState> = server.state

    private val _files = MutableStateFlow<List<SharedFile>>(emptyList())
    val files: StateFlow<List<SharedFile>> = _files.asStateFlow()

    fun addFiles(picked: List<SharedFile>) {
        if (picked.isEmpty()) return
        val existing = _files.value.mapTo(mutableSetOf()) { it.locator }
        val added = picked.filterNot { it.locator in existing }
        if (added.isEmpty()) return
        val updated = added + _files.value
        _files.value = updated
        server.startShare(updated)
    }

    fun removeFile(id: String) {
        val updated = _files.value.filterNot { it.id == id }
        _files.value = updated
        if (updated.isEmpty()) server.stop() else server.startShare(updated)
    }

    fun restoreFile(file: SharedFile, index: Int) {
        val current = _files.value
        val updated = current.toMutableList().apply {
            add(index.coerceIn(0, size), file)
        }
        _files.value = updated
        server.startShare(updated)
    }

    fun retry() {
        val current = _files.value
        if (current.isNotEmpty()) server.startShare(current)
    }

    override fun onCleared() {
        server.stop()
    }
}
