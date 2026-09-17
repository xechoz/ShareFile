package com.xechoz.sharefile.ui.remote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xechoz.sharefile.model.RemoteFile
import com.xechoz.sharefile.net.FileDownloader
import com.xechoz.sharefile.net.RemoteError
import com.xechoz.sharefile.net.RemoteException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RemoteUiState {
    data object Loading : RemoteUiState
    data class Loaded(
        val files: List<RemoteFile>,
        val selected: Set<String>,
    ) : RemoteUiState

    data class Downloading(val current: Int, val total: Int) : RemoteUiState
    data class Done(val count: Int) : RemoteUiState
    data class Error(val error: RemoteError) : RemoteUiState
}

class RemoteFilesViewModel(
    private val client: FileDownloader,
) : ViewModel() {

    private val _state = MutableStateFlow<RemoteUiState>(RemoteUiState.Loading)
    val state: StateFlow<RemoteUiState> = _state.asStateFlow()

    private var shareUrl: String = ""

    fun load(url: String) {
        shareUrl = url.trim()
        _state.value = RemoteUiState.Loading
        viewModelScope.launch {
            _state.value = try {
                val files = client.fetchFiles(shareUrl)
                RemoteUiState.Loaded(files, files.map { it.id }.toSet())
            } catch (e: RemoteException) {
                RemoteUiState.Error(RemoteError.Server(e.code))
            } catch (e: Exception) {
                RemoteUiState.Error(RemoteError.LoadFailed)
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
                    client.download(shareUrl, file)
                }
                _state.value = RemoteUiState.Done(targets.size)
            } catch (e: RemoteException) {
                _state.value = RemoteUiState.Error(RemoteError.Server(e.code))
            } catch (e: Exception) {
                _state.value = RemoteUiState.Error(RemoteError.DownloadFailed)
            }
        }
    }
}
