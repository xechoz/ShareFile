package com.xechoz.sharefile.ui.share

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xechoz.sharefile.model.SharedFile
import com.xechoz.sharefile.server.ServerState
import com.xechoz.sharefile.server.formatSize
import com.xechoz.sharefile.ui.components.DoubleBackHandler
import com.xechoz.sharefile.ui.components.FileRow
import com.xechoz.sharefile.ui.components.QrCard
import com.xechoz.sharefile.ui.components.ServerErrorCard
import com.xechoz.sharefile.ui.theme.PillShape
import com.xechoz.sharefile.ui.theme.ShareFileTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareScreen(
    onBack: () -> Unit,
    viewModel: ShareViewModel = viewModel(),
) {
    val files by viewModel.files.collectAsStateWithLifecycle()
    val serverState by viewModel.serverState.collectAsStateWithLifecycle()

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris -> viewModel.addFiles(uris) }

    ShareContent(
        files = files,
        serverState = serverState,
        onBack = onBack,
        onPickFiles = { picker.launch(arrayOf("*/*")) },
        onRemoveFile = viewModel::removeFile,
        onRestoreFile = viewModel::restoreFile,
        onClearAll = viewModel::clearAll,
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
    onClearAll: () -> Unit,
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
            when (serverState) {
                is ServerState.Running -> {
                    QrCard(
                        title = "Scan to download",
                        url = serverState.url,
                        onCopied = {
                            scope.launch { snackbarHostState.showSnackbar("Link copied") }
                        },
                    )
                    Spacer(Modifier.height(16.dp))
                }

                is ServerState.Error -> {
                    ServerErrorCard(message = serverState.message, onRetry = onRetry)
                    Spacer(Modifier.height(16.dp))
                }

                ServerState.Stopped -> Unit
            }

            if (files.isEmpty()) {
                EmptyHint(onPickFiles = onPickFiles)
            } else {
                FileListHeader(
                    count = files.size,
                    totalSize = files.sumOf { it.size },
                    onClearAll = onClearAll,
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(files) { index, file ->
                        FileRow(
                            name = file.name,
                            size = file.size,
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
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onPickFiles,
                    modifier = Modifier.fillMaxWidth(),
                    shape = PillShape,
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Add more files")
                }
            }
        }
    }
}

@Composable
private fun FileListHeader(
    count: Int,
    totalSize: Long,
    onClearAll: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$count ${if (count == 1) "file" else "files"} · ${formatSize(totalSize)}",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onClearAll, shape = PillShape) {
            Text("Clear all")
        }
    }
}

@Composable
private fun EmptyHint(onPickFiles: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Icon(
                Icons.Default.Upload,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No files yet",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Select files, then let the other device scan the QR code or open the link to download.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onPickFiles, shape = PillShape) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Select files")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ShareScreenPreview() {
    ShareFileTheme {
        ShareContent(
            files = emptyList(),
            serverState = ServerState.Stopped,
            onBack = {},
            onPickFiles = {},
            onRemoveFile = {},
            onRestoreFile = { _, _ -> },
            onClearAll = {},
            onRetry = {},
        )
    }
}
