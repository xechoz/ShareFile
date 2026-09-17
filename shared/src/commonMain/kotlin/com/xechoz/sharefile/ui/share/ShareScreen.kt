package com.xechoz.sharefile.ui.share

import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.xechoz.sharefile.platform.FirewallStatus
import com.xechoz.sharefile.platform.LocalAppContainer
import com.xechoz.sharefile.platform.fileDropTarget
import com.xechoz.sharefile.platform.rememberFileDropState
import com.xechoz.sharefile.platform.rememberFilePicker
import com.xechoz.sharefile.server.ServerState
import com.xechoz.sharefile.ui.components.ConnectionCard
import com.xechoz.sharefile.ui.components.ContentContainer
import com.xechoz.sharefile.ui.components.DoubleBackHandler
import com.xechoz.sharefile.ui.components.EmptyFileHint
import com.xechoz.sharefile.ui.components.FileListHeader
import com.xechoz.sharefile.ui.components.FileRow
import com.xechoz.sharefile.ui.components.FirewallCard
import com.xechoz.sharefile.ui.components.QrCard
import com.xechoz.sharefile.ui.components.ServerErrorCard
import com.xechoz.sharefile.ui.icons.AppIcons
import com.xechoz.sharefile.ui.layout.LocalWindowLayout
import com.xechoz.sharefile.ui.layout.WindowLayout
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
    val viewModel: ShareViewModel = viewModel {
        ShareViewModel(container.newFileServer(), container.firewall)
    }
    val files by viewModel.files.collectAsStateWithLifecycle()
    val serverState by viewModel.serverState.collectAsStateWithLifecycle()
    val firewallStatus by viewModel.firewallStatus.collectAsStateWithLifecycle()
    val isAllowingFirewall by viewModel.allowingFirewall.collectAsStateWithLifecycle()
    val firewallCommand by viewModel.firewallCommand.collectAsStateWithLifecycle()

    val pickFiles = rememberFilePicker(viewModel::addFiles)

    ShareContent(
        files = files,
        serverState = serverState,
        firewallStatus = firewallStatus,
        isAllowingFirewall = isAllowingFirewall,
        firewallCommand = firewallCommand,
        onBack = onBack,
        onPickFiles = pickFiles,
        onFilesDropped = viewModel::addFiles,
        onRemoveFile = viewModel::removeFile,
        onRestoreFile = viewModel::restoreFile,
        onRetry = viewModel::retry,
        onAllowFirewall = viewModel::allowFirewall,
        preferUrl = container.platform.prefersUrlConnection,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShareContent(
    files: List<SharedFile>,
    serverState: ServerState,
    firewallStatus: FirewallStatus,
    isAllowingFirewall: Boolean,
    firewallCommand: String?,
    onBack: () -> Unit,
    onPickFiles: () -> Unit,
    onFilesDropped: (List<SharedFile>) -> Unit,
    onRemoveFile: (String) -> Unit,
    onRestoreFile: (SharedFile, Int) -> Unit,
    onRetry: () -> Unit,
    onAllowFirewall: () -> Unit,
    preferUrl: Boolean,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val dropState = rememberFileDropState()

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
                        Icon(AppIcons.ArrowLeft, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        ContentContainer(
            modifier = Modifier
                .padding(padding)
                .fileDropTarget(dropState, onFilesDropped)
                .then(
                    if (dropState.isActive) {
                        Modifier.border(2.dp, MaterialTheme.colorScheme.primary)
                    } else {
                        Modifier
                    },
                ),
            maxWidth = 960.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                when (LocalWindowLayout.current) {
                    WindowLayout.Expanded -> ExpandedShare(
                        files = files,
                        serverState = serverState,
                        firewallStatus = firewallStatus,
                        isAllowingFirewall = isAllowingFirewall,
                        firewallCommand = firewallCommand,
                        snackbarHostState = snackbarHostState,
                        onPickFiles = onPickFiles,
                        onRemoveFile = onRemoveFile,
                        onRestoreFile = onRestoreFile,
                        onRetry = onRetry,
                        onAllowFirewall = onAllowFirewall,
                        preferUrl = preferUrl,
                    )

                    WindowLayout.Compact -> CompactShare(
                        files = files,
                        serverState = serverState,
                        firewallStatus = firewallStatus,
                        isAllowingFirewall = isAllowingFirewall,
                        firewallCommand = firewallCommand,
                        snackbarHostState = snackbarHostState,
                        onPickFiles = onPickFiles,
                        onRemoveFile = onRemoveFile,
                        onRestoreFile = onRestoreFile,
                        onRetry = onRetry,
                        onAllowFirewall = onAllowFirewall,
                        preferUrl = preferUrl,
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandedShare(
    files: List<SharedFile>,
    serverState: ServerState,
    firewallStatus: FirewallStatus,
    isAllowingFirewall: Boolean,
    firewallCommand: String?,
    snackbarHostState: SnackbarHostState,
    onPickFiles: () -> Unit,
    onRemoveFile: (String) -> Unit,
    onRestoreFile: (SharedFile, Int) -> Unit,
    onRetry: () -> Unit,
    onAllowFirewall: () -> Unit,
    preferUrl: Boolean,
) {
    val scope = rememberCoroutineScope()
    if (files.isEmpty()) {
        Column(modifier = Modifier.fillMaxSize()) {
            EmptyFiles(onPickFiles = onPickFiles, modifier = Modifier.weight(1f))
            Spacer(Modifier.height(8.dp))
            AddFilesButton(isEmpty = true, onPickFiles = onPickFiles)
        }
        return
    }
    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            ServerStatus(
                serverState = serverState,
                onRetry = onRetry,
                onCopied = { scope.launch { snackbarHostState.showSnackbar("Link copied") } },
                preferUrl = preferUrl,
            )
            if (firewallStatus !is FirewallStatus.Allowed) {
                Spacer(Modifier.height(12.dp))
                FirewallCard(
                    status = firewallStatus,
                    isAllowing = isAllowingFirewall,
                    command = firewallCommand,
                    onAllow = onAllowFirewall,
                    onCopied = { scope.launch { snackbarHostState.showSnackbar("Command copied") } },
                )
            }
        }
        Spacer(Modifier.width(24.dp))
        Column(modifier = Modifier.weight(1.4f)) {
            FileList(
                files = files,
                snackbarHostState = snackbarHostState,
                onRemoveFile = onRemoveFile,
                onRestoreFile = onRestoreFile,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.height(8.dp))
            AddFilesButton(isEmpty = false, onPickFiles = onPickFiles)
        }
    }
}

@Composable
private fun CompactShare(
    files: List<SharedFile>,
    serverState: ServerState,
    firewallStatus: FirewallStatus,
    isAllowingFirewall: Boolean,
    firewallCommand: String?,
    snackbarHostState: SnackbarHostState,
    onPickFiles: () -> Unit,
    onRemoveFile: (String) -> Unit,
    onRestoreFile: (SharedFile, Int) -> Unit,
    onRetry: () -> Unit,
    onAllowFirewall: () -> Unit,
    preferUrl: Boolean,
) {
    val scope = rememberCoroutineScope()
    Column(modifier = Modifier.fillMaxSize()) {
        if (files.isNotEmpty()) {
            ServerStatus(
                serverState = serverState,
                onRetry = onRetry,
                onCopied = { scope.launch { snackbarHostState.showSnackbar("Link copied") } },
                preferUrl = preferUrl,
            )
            if (firewallStatus !is FirewallStatus.Allowed) {
                Spacer(Modifier.height(8.dp))
                FirewallCard(
                    status = firewallStatus,
                    isAllowing = isAllowingFirewall,
                    command = firewallCommand,
                    onAllow = onAllowFirewall,
                    onCopied = { scope.launch { snackbarHostState.showSnackbar("Command copied") } },
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        if (files.isEmpty()) {
            EmptyFiles(onPickFiles = onPickFiles, modifier = Modifier.weight(1f))
        } else {
            FileList(
                files = files,
                snackbarHostState = snackbarHostState,
                onRemoveFile = onRemoveFile,
                onRestoreFile = onRestoreFile,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(8.dp))
        AddFilesButton(isEmpty = files.isEmpty(), onPickFiles = onPickFiles)
    }
}

@Composable
private fun FileList(
    files: List<SharedFile>,
    snackbarHostState: SnackbarHostState,
    onRemoveFile: (String) -> Unit,
    onRestoreFile: (SharedFile, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    LazyColumn(modifier = modifier) {
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
                        Icon(AppIcons.Close, contentDescription = "Remove")
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
}

@Composable
private fun EmptyFiles(
    onPickFiles: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EmptyFileHint(
        icon = AppIcons.Upload,
        title = "No files yet",
        description = "Drag files here or click Select files, then let the other device scan the QR code or open the link to download.",
        modifier = modifier,
    )
}

@Composable
private fun AddFilesButton(
    isEmpty: Boolean,
    onPickFiles: () -> Unit,
) {
    Button(
        onClick = onPickFiles,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = PillShape,
    ) {
        Icon(AppIcons.Add, contentDescription = null)
        Spacer(Modifier.size(8.dp))
        Text(
            text = if (isEmpty) "Select files" else "Add more files",
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun ServerStatus(
    serverState: ServerState,
    onRetry: () -> Unit,
    onCopied: () -> Unit,
    preferUrl: Boolean,
) {
    when (serverState) {
        is ServerState.Running -> {
            val expanded = LocalWindowLayout.current == WindowLayout.Expanded
            if (preferUrl || expanded) {
                ConnectionCard(
                    url = serverState.url,
                    onCopied = onCopied,
                    qrSize = if (expanded) 140.dp else 110.dp,
                )
            } else {
                QrCard(
                    url = serverState.url,
                    onCopied = onCopied,
                )
            }
        }

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
            firewallStatus = FirewallStatus.Allowed,
            isAllowingFirewall = false,
            firewallCommand = null,
            onBack = {},
            onPickFiles = {},
            onFilesDropped = {},
            onRemoveFile = {},
            onRestoreFile = { _, _ -> },
            onRetry = {},
            onAllowFirewall = {},
            preferUrl = false,
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
            firewallStatus = FirewallStatus.Blocked,
            isAllowingFirewall = false,
            firewallCommand = "sudo ufw allow from 192.168.1.0/24 to any port 8080 proto tcp",
            onBack = {},
            onPickFiles = {},
            onFilesDropped = {},
            onRemoveFile = {},
            onRestoreFile = { _, _ -> },
            onRetry = {},
            onAllowFirewall = {},
            preferUrl = false,
        )
    }
}
