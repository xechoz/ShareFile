package com.xechoz.sharefile.ui.receive

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.xechoz.sharefile.platform.DownloadFolderChooser
import com.xechoz.sharefile.platform.FirewallStatus
import com.xechoz.sharefile.platform.LocalAppContainer
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
import com.xechoz.sharefile.ui.layout.LocalWindowLayout
import com.xechoz.sharefile.ui.layout.WindowLayout
import com.xechoz.sharefile.ui.theme.ShareFileTheme
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ReceiveScreen(
    onBack: () -> Unit,
) {
    val container = LocalAppContainer.current
    val viewModel: ReceiveViewModel = viewModel {
        ReceiveViewModel(container.newFileServer(), container.firewall)
    }
    val serverState by viewModel.serverState.collectAsStateWithLifecycle()
    val received by viewModel.received.collectAsStateWithLifecycle()
    val firewallStatus by viewModel.firewallStatus.collectAsStateWithLifecycle()
    val isAllowingFirewall by viewModel.allowingFirewall.collectAsStateWithLifecycle()
    val firewallCommand by viewModel.firewallCommand.collectAsStateWithLifecycle()

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

    val chooser = container.platform as? DownloadFolderChooser

    ReceiveContent(
        serverState = serverState,
        received = received,
        firewallStatus = firewallStatus,
        isAllowingFirewall = isAllowingFirewall,
        firewallCommand = firewallCommand,
        downloadFolderName = container.platform.downloadFolderName,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onOpen = open,
        onShare = share,
        onRetry = viewModel::retry,
        onAllowFirewall = viewModel::allowFirewall,
        onOpenFolder = {
            if (!container.platform.openDownloadFolder()) {
                scope.launch { snackbarHostState.showSnackbar("No file manager found") }
            }
        },
        onChooseFolder = chooser?.let { target -> { target.chooseDownloadFolder() } },
        preferUrl = container.platform.prefersUrlConnection,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReceiveContent(
    serverState: ServerState,
    received: List<ReceivedFile>,
    firewallStatus: FirewallStatus,
    isAllowingFirewall: Boolean,
    firewallCommand: String?,
    downloadFolderName: String,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onOpen: (ReceivedFile) -> Unit,
    onShare: (ReceivedFile) -> Unit,
    onRetry: () -> Unit,
    onAllowFirewall: () -> Unit,
    onOpenFolder: () -> Unit,
    onChooseFolder: (() -> Unit)?,
    preferUrl: Boolean,
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
                "${fresh.first().name} saved to $downloadFolderName"
            } else {
                "${fresh.size} files saved to $downloadFolderName"
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
                actions = {
                    FolderMenu(
                        folderName = downloadFolderName,
                        onOpenFolder = onOpenFolder,
                        onChooseFolder = onChooseFolder,
                    )
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        ContentContainer(
            modifier = Modifier.padding(padding),
            maxWidth = 960.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                when (LocalWindowLayout.current) {
                    WindowLayout.Expanded -> Row(modifier = Modifier.fillMaxSize()) {
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
                            FirewallSection(
                                firewallStatus = firewallStatus,
                                isAllowingFirewall = isAllowingFirewall,
                                firewallCommand = firewallCommand,
                                onAllowFirewall = onAllowFirewall,
                                onCopied = { scope.launch { snackbarHostState.showSnackbar("Command copied") } },
                            )
                        }
                        Spacer(Modifier.width(24.dp))
                        ReceivedBody(
                            received = received,
                            onOpen = onOpen,
                            onLongPress = { sheetFile = it },
                            modifier = Modifier.weight(1.4f),
                        )
                    }

                    WindowLayout.Compact -> Column(modifier = Modifier.fillMaxSize()) {
                        ServerStatus(
                            serverState = serverState,
                            onRetry = onRetry,
                            onCopied = { scope.launch { snackbarHostState.showSnackbar("Link copied") } },
                            preferUrl = preferUrl,
                        )
                        FirewallSection(
                            firewallStatus = firewallStatus,
                            isAllowingFirewall = isAllowingFirewall,
                            firewallCommand = firewallCommand,
                            onAllowFirewall = onAllowFirewall,
                            onCopied = { scope.launch { snackbarHostState.showSnackbar("Command copied") } },
                        )
                        Spacer(Modifier.height(8.dp))
                        ReceivedBody(
                            received = received,
                            onOpen = onOpen,
                            onLongPress = { sheetFile = it },
                            modifier = Modifier.weight(1f),
                        )
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
private fun FirewallSection(
    firewallStatus: FirewallStatus,
    isAllowingFirewall: Boolean,
    firewallCommand: String?,
    onAllowFirewall: () -> Unit,
    onCopied: () -> Unit,
) {
    if (firewallStatus is FirewallStatus.Allowed) return
    Spacer(Modifier.height(12.dp))
    FirewallCard(
        status = firewallStatus,
        isAllowing = isAllowingFirewall,
        command = firewallCommand,
        onAllow = onAllowFirewall,
        onCopied = onCopied,
    )
}

@Composable
private fun ReceivedBody(
    received: List<ReceivedFile>,
    onOpen: (ReceivedFile) -> Unit,
    onLongPress: (ReceivedFile) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (received.isEmpty()) {
        EmptyFileHint(
            icon = Icons.Default.Download,
            title = "Waiting for uploads…",
            description = "Files sent from the other device will appear here.",
            modifier = modifier,
        )
        return
    }
    LazyColumn(modifier = modifier) {
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
                    onLongClick = { onLongPress(file) },
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

@Composable
private fun FolderMenu(
    folderName: String,
    onOpenFolder: () -> Unit,
    onChooseFolder: (() -> Unit)?,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(
            onClick = {
                if (onChooseFolder == null) onOpenFolder() else expanded = true
            },
        ) {
            Icon(Icons.Default.FolderOpen, contentDescription = "Open save folder")
        }
        if (onChooseFolder != null) {
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text("Open $folderName folder") },
                    onClick = {
                        expanded = false
                        onOpenFolder()
                    },
                )
                DropdownMenuItem(
                    text = { Text("Change save folder…") },
                    onClick = {
                        expanded = false
                        onChooseFolder()
                    },
                )
            }
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
            firewallStatus = FirewallStatus.Allowed,
            isAllowingFirewall = false,
            firewallCommand = null,
            downloadFolderName = "Downloads",
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onOpen = {},
            onShare = {},
            onRetry = {},
            onAllowFirewall = {},
            onOpenFolder = {},
            onChooseFolder = null,
            preferUrl = false,
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
            firewallStatus = FirewallStatus.Allowed,
            isAllowingFirewall = false,
            firewallCommand = null,
            downloadFolderName = "Downloads",
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onOpen = {},
            onShare = {},
            onRetry = {},
            onAllowFirewall = {},
            onOpenFolder = {},
            onChooseFolder = null,
            preferUrl = false,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReceiveScreenDesktopPreview() {
    ShareFileTheme {
        ReceiveContent(
            serverState = ServerState.Running(url = "http://192.168.1.42:8080", port = 8080),
            received = listOf(
                ReceivedFile(
                    name = "vacation.jpg",
                    size = 3_200_000,
                    savedPath = "/home/user/Downloads/vacation.jpg",
                ),
            ),
            firewallStatus = FirewallStatus.Allowed,
            isAllowingFirewall = false,
            firewallCommand = null,
            downloadFolderName = "Downloads",
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onOpen = {},
            onShare = {},
            onRetry = {},
            onAllowFirewall = {},
            onOpenFolder = {},
            onChooseFolder = {},
            preferUrl = true,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReceiveScreenFirewallPreview() {
    ShareFileTheme {
        ReceiveContent(
            serverState = ServerState.Running(url = "http://192.168.1.42:8080", port = 8080),
            received = emptyList(),
            firewallStatus = FirewallStatus.Blocked,
            isAllowingFirewall = false,
            firewallCommand = "sudo ufw allow from 192.168.1.0/24 to any port 8080 proto tcp",
            downloadFolderName = "Downloads",
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onOpen = {},
            onShare = {},
            onRetry = {},
            onAllowFirewall = {},
            onOpenFolder = {},
            onChooseFolder = null,
            preferUrl = false,
        )
    }
}
