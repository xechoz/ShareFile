package com.xechoz.sharefile.ui.receive

import android.net.Uri
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xechoz.sharefile.model.ReceivedFile
import com.xechoz.sharefile.server.ServerState
import com.xechoz.sharefile.ui.components.DoubleBackHandler
import com.xechoz.sharefile.ui.components.EmptyFileHint
import com.xechoz.sharefile.ui.components.FileListHeader
import com.xechoz.sharefile.ui.components.FileRow
import com.xechoz.sharefile.ui.components.QrCard
import com.xechoz.sharefile.ui.components.ServerErrorCard
import com.xechoz.sharefile.ui.theme.ShareFileTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveScreen(
    onBack: () -> Unit,
    viewModel: ReceiveViewModel = viewModel(),
) {
    val serverState by viewModel.serverState.collectAsStateWithLifecycle()
    val received by viewModel.received.collectAsStateWithLifecycle()

    ReceiveContent(
        serverState = serverState,
        received = received,
        onBack = onBack,
        onRetry = viewModel::retry,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReceiveContent(
    serverState: ServerState,
    received: List<ReceivedFile>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    DoubleBackHandler(
        message = "Tap again to exit receiving",
        onBack = onBack,
        showMessage = { snackbarHostState.showSnackbar(it) },
    )

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
                            uri = Uri.parse(file.savedPath),
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
            onBack = {},
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
            onBack = {},
            onRetry = {},
        )
    }
}
