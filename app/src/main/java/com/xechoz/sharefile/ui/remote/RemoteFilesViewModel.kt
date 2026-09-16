package com.xechoz.sharefile.ui.remote

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xechoz.sharefile.model.RemoteFile
import com.xechoz.sharefile.net.RemoteClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface RemoteUiState {
    data object Loading : RemoteUiState
    data class Loaded(
        val files: List<RemoteFile>,
        val selected: Set<String>,
    ) : RemoteUiState
    data class Downloading(val current: Int, val total: Int) : RemoteUiState
    data class Done(val count: Int) : RemoteUiState
    data class Error(val message: String) : RemoteUiState
}

class RemoteFilesViewModel(app: Application) : AndroidViewModel(app) {

    private val client = RemoteClient(app)

    private val _state = MutableStateFlow<RemoteUiState>(RemoteUiState.Loading)
    val state: StateFlow<RemoteUiState> = _state.asStateFlow()

    private var shareUrl: String = ""

    fun load(url: String) {
        shareUrl = url.trim()
        _state.value = RemoteUiState.Loading
        viewModelScope.launch {
            _state.value = try {
                val files = withContext(Dispatchers.IO) { client.fetchFiles(shareUrl) }
                RemoteUiState.Loaded(files, files.map { it.id }.toSet())
            } catch (e: Exception) {
                RemoteUiState.Error(e.message ?: "Failed to load files")
            }
        }
    }

    fun toggle(id: String) {
        val current = _state.value as? RemoteUiState.Loaded ?: return
        val selected = if (id in current.selected) current.selected - id else current.selected + id
        _state.value = current.copy(selected = selected)
    }

    fun downloadSelected() {
        val current = _state.value as? RemoteUiState.Loaded ?: return
        val targets = current.files.filter { it.id in current.selected }
        if (targets.isEmpty()) return
        viewModelScope.launch {
            try {
                targets.forEachIndexed { index, file ->
                    _state.value = RemoteUiState.Downloading(index + 1, targets.size)
                    withContext(Dispatchers.IO) { client.download(shareUrl, file) }
                }
                _state.value = RemoteUiState.Done(targets.size)
            } catch (e: Exception) {
                _state.value = RemoteUiState.Error(e.message ?: "Download failed")
            }
        }
    }
}
