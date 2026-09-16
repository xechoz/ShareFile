package com.xechoz.sharefile.ui.share

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xechoz.sharefile.model.SharedFile
import com.xechoz.sharefile.platform.LocalAppContainer
import com.xechoz.sharefile.platform.rememberFilePicker
import com.xechoz.sharefile.server.ServerState
import com.xechoz.sharefile.ui.components.DoubleBackHandler
import com.xechoz.sharefile.ui.components.EmptyFileHint
import com.xechoz.sharefile.ui.components.FileListHeader
import com.xechoz.sharefile.ui.components.FileRow
import com.xechoz.sharefile.ui.components.QrCard
import com.xechoz.sharefile.ui.components.ServerErrorCard
import com.xechoz.sharefile.ui.theme.PillShape
import com.xechoz.sharefile.ui.theme.ShareFileTheme
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareScreen(
    onBack: () -> Unit,
) {
    val container = LocalAppContainer.current
    val viewModel: ShareViewModel = viewModel { ShareViewModel(container.newFileServer()) }
    val files by viewModel.files.collectAsStateWithLifecycle()
    val serverState by viewModel.serverState.collectAsStateWithLifecycle()

    val pickFiles = rememberFilePicker(viewModel::addFiles)

    ShareContent(
        files = files,
        serverState = serverState,
        onBack = onBack,
        onPickFiles = pickFiles,
        onRemoveFile = viewModel::removeFile,
        onRestoreFile = viewModel::restoreFile,
        onRetry = viewModel::retry,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShareContent(
    files: List<SharedFile>,
    serverState: ServerState,
    onBack: () -> Unit,
    onPickFiles: () -> Unit,
    onRemoveFile: (String) -> Unit,
    onRestoreFile: (SharedFile, Int) -> Unit,
    onRetry: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    DoubleBackHandler(
        message = "Tap again to exit sharing",
        onBack = onBack,
        showMessage = { snackbarHostState.showSnackbar(it) },
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Share") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            if (files.isNotEmpty()) {
                ServerStatus(serverState = serverState, onRetry = onRetry, onCopied = {
                    scope.launch { snackbarHostState.showSnackbar("Link copied") }
                })
                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f),
                ) {
                    item {
                        FileListHeader(
                            count = files.size,
                            totalSize = files.sumOf { it.size },
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    itemsIndexed(files) { index, file ->
                        FileRow(
                            name = file.name,
                            size = file.size,
                            locator = file.locator,
                            trailing = {
                                IconButton(onClick = {
                                    onRemoveFile(file.id)
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Removed ${file.name}",
                                            actionLabel = "Undo",
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            onRestoreFile(file, index)
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove")
                                }
                            },
                        )
                        if (index < files.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 68.dp),
                                color = MaterialTheme.colorScheme.outlineVariant,
                            )
                        }
                    }
                }
            } else {
                EmptyFileHint(
                    icon = Icons.Default.Upload,
                    title = "No files yet",
                    description = "Select files, then let the other device scan the QR code or open the link to download.",
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onPickFiles,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = PillShape,
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text(
                    text = if (files.isEmpty()) "Select files" else "Add more files",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
private fun ServerStatus(
    serverState: ServerState,
    onRetry: () -> Unit,
    onCopied: () -> Unit,
) {
    when (serverState) {
        is ServerState.Running -> QrCard(
            title = "Scan to download",
            url = serverState.url,
            onCopied = onCopied,
        )

        is ServerState.Error -> ServerErrorCard(message = serverState.message, onRetry = onRetry)

        ServerState.Stopped -> Unit
    }
}

@Preview(showBackground = true)
@Composable
private fun ShareScreenPreview() {
    ShareFileTheme {
        ShareContent(
            files = listOf(
                SharedFile(
                    id = "1",
                    locator = "content://preview/photo.jpg",
                    name = "photo.jpg",
                    size = 2_400_000,
                ),
                SharedFile(
                    id = "2",
                    locator = "content://preview/report.pdf",
                    name = "report.pdf",
                    size = 512_000,
                ),
            ),
            serverState = ServerState.Stopped,
            onBack = {},
            onPickFiles = {},
            onRemoveFile = {},
            onRestoreFile = { _, _ -> },
            onRetry = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ShareScreenRunningPreview() {
    ShareFileTheme {
        ShareContent(
            files = emptyList(),
            serverState = ServerState.Running(url = "http://192.168.1.42:8080", port = 8080),
            onBack = {},
            onPickFiles = {},
            onRemoveFile = {},
            onRestoreFile = { _, _ -> },
            onRetry = {},
        )
    }
}
