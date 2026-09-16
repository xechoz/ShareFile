package com.xechoz.sharefile.ui.share

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xechoz.sharefile.model.SharedFile
import com.xechoz.sharefile.server.FileServer
import com.xechoz.sharefile.server.ServerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class ShareViewModel(app: Application) : AndroidViewModel(app) {

    private val server = FileServer(app)

    val serverState: StateFlow<ServerState> = server.state

    private val _files = MutableStateFlow<List<SharedFile>>(emptyList())
    val files: StateFlow<List<SharedFile>> = _files.asStateFlow()

    fun addFiles(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val added = withContext(Dispatchers.IO) { uris.mapNotNull { toSharedFile(it) } }
            val updated = _files.value + added
            _files.value = updated
            server.startShare(updated)
        }
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

    fun clearAll() {
        _files.value = emptyList()
        server.stop()
    }

    fun retry() {
        val current = _files.value
        if (current.isNotEmpty()) server.startShare(current)
    }

    private fun toSharedFile(uri: Uri): SharedFile? {
        val resolver = getApplication<Application>().contentResolver
        val name = resolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
        } ?: uri.lastPathSegment ?: "file"
        val size = resolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (index >= 0 && cursor.moveToFirst()) cursor.getLong(index) else 0L
        } ?: 0L
        return SharedFile(id = UUID.randomUUID().toString(), uri = uri, name = name, size = size)
    }

    override fun onCleared() {
        server.stop()
    }
}
