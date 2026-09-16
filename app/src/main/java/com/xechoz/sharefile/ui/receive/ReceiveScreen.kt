package com.xechoz.sharefile.ui.receive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xechoz.sharefile.model.ReceivedFile
import com.xechoz.sharefile.server.ServerState
import com.xechoz.sharefile.ui.components.DoubleBackHandler
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
            when (serverState) {
                is ServerState.Running -> {
                    QrCard(
                        title = "Scan to send files",
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

            Text(
                text = if (received.isEmpty()) {
                    "Received files"
                } else {
                    "Received files · ${received.size}"
                },
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(8.dp))

            if (received.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp),
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Waiting for uploads…",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Files sent from the other device will appear here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(received, key = { it.savedPath }) { file ->
                        FileRow(
                            name = file.name,
                            size = file.size,
                            leading = {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(24.dp),
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReceiveScreenPreview() {
    ShareFileTheme {
        ReceiveContent(
            serverState = ServerState.Stopped,
            received = emptyList(),
            onBack = {},
            onRetry = {},
        )
    }
}
