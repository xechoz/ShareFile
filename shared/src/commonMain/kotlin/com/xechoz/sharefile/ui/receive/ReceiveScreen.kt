package com.xechoz.sharefile.ui.receive

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xechoz.sharefile.model.ReceivedFile
import com.xechoz.sharefile.platform.LocalAppContainer
import com.xechoz.sharefile.server.ServerState
import com.xechoz.sharefile.ui.components.DoubleBackHandler
import com.xechoz.sharefile.ui.components.EmptyFileHint
import com.xechoz.sharefile.ui.components.FileListHeader
import com.xechoz.sharefile.ui.components.FileRow
import com.xechoz.sharefile.ui.components.QrCard
import com.xechoz.sharefile.ui.components.ServerErrorCard
import com.xechoz.sharefile.ui.theme.ShareFileTheme
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ReceiveScreen(
    onBack: () -> Unit,
) {
    val container = LocalAppContainer.current
    val viewModel: ReceiveViewModel = viewModel { ReceiveViewModel(container.newFileServer()) }
    val serverState by viewModel.serverState.collectAsStateWithLifecycle()
    val received by viewModel.received.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val share: (ReceivedFile) -> Unit = { container.platform.shareFile(it) }

    val open: (ReceivedFile) -> Unit = { file ->
        if (!container.platform.openFile(file)) {
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "No app can open ${file.name}",
                    actionLabel = "Share",
                )
                if (result == SnackbarResult.ActionPerformed) share(file)
            }
        }
    }

    ReceiveContent(
        serverState = serverState,
        received = received,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onOpen = open,
        onShare = share,
        onRetry = viewModel::retry,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReceiveContent(
    serverState: ServerState,
    received: List<ReceivedFile>,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onOpen: (ReceivedFile) -> Unit,
    onShare: (ReceivedFile) -> Unit,
    onRetry: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var sheetFile by remember { mutableStateOf<ReceivedFile?>(null) }

    DoubleBackHandler(
        message = "Tap again to exit receiving",
        onBack = onBack,
        showMessage = { snackbarHostState.showSnackbar(it) },
    )

    val seen = remember { received.mapTo(mutableSetOf()) { it.savedPath } }
    LaunchedEffect(received) {
        val fresh = received.filter { seen.add(it.savedPath) }
        if (fresh.isEmpty()) return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = if (fresh.size == 1) {
                "${fresh.first().name} saved to Downloads"
            } else {
                "${fresh.size} files saved to Downloads"
            },
            actionLabel = if (fresh.size == 1) "Open" else null,
        )
        if (result == SnackbarResult.ActionPerformed) onOpen(fresh.first())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receive") },
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
            ServerStatus(serverState = serverState, onRetry = onRetry, onCopied = {
                scope.launch { snackbarHostState.showSnackbar("Link copied") }
            })
            Spacer(Modifier.height(8.dp))
            if (received.isEmpty()) {
                EmptyFileHint(
                    icon = Icons.Default.Download,
                    title = "Waiting for uploads…",
                    description = "Files sent from the other device will appear here.",
                    modifier = Modifier.weight(1f),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                ) {
                    item {
                        FileListHeader(
                            count = received.size,
                            totalSize = received.sumOf { it.size },
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    itemsIndexed(received, key = { _, file -> file.savedPath }) { index, file ->
                        FileRow(
                            name = file.name,
                            size = file.size,
                            modifier = Modifier.combinedClickable(
                                onClick = { onOpen(file) },
                                onLongClick = { sheetFile = file },
                            ),
                            locator = file.savedPath,
                            trailing = {
                                Icon(
                                    Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = "Open",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                        )
                        if (index < received.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 68.dp),
                                color = MaterialTheme.colorScheme.outlineVariant,
                            )
                        }
                    }
                }
            }
        }
    }

    sheetFile?.let { file ->
        ModalBottomSheet(onDismissRequest = { sheetFile = null }) {
            FileActionRow(
                icon = Icons.AutoMirrored.Filled.OpenInNew,
                label = "Open",
                onClick = {
                    sheetFile = null
                    onOpen(file)
                },
            )
            FileActionRow(
                icon = Icons.Default.Share,
                label = "Share",
                onClick = {
                    sheetFile = null
                    onShare(file)
                },
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FileActionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = { Icon(icon, contentDescription = null) },
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
private fun ServerStatus(
    serverState: ServerState,
    onRetry: () -> Unit,
    onCopied: () -> Unit,
) {
    when (serverState) {
        is ServerState.Running -> QrCard(
            title = "Scan to send files",
            url = serverState.url,
            onCopied = onCopied,
        )

        is ServerState.Error -> ServerErrorCard(message = serverState.message, onRetry = onRetry)

        ServerState.Stopped -> Unit
    }
}

@Preview(showBackground = true)
@Composable
private fun ReceiveScreenPreview() {
    ShareFileTheme {
        ReceiveContent(
            serverState = ServerState.Running(url = "http://192.168.1.42:8080", port = 8080),
            received = listOf(
                ReceivedFile(
                    name = "vacation.jpg",
                    size = 3_200_000,
                    savedPath = "content://media/external/downloads/1",
                ),
                ReceivedFile(
                    name = "notes.pdf",
                    size = 128_000,
                    savedPath = "content://media/external/downloads/2",
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onOpen = {},
            onShare = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReceiveScreenEmptyPreview() {
    ShareFileTheme {
        ReceiveContent(
            serverState = ServerState.Stopped,
            received = emptyList(),
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onOpen = {},
            onShare = {},
            onRetry = {},
        )
    }
}
