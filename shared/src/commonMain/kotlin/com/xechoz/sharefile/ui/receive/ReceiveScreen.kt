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
import com.xechoz.sharefile.resources.Res
import com.xechoz.sharefile.resources.action_back
import com.xechoz.sharefile.resources.action_open
import com.xechoz.sharefile.resources.action_receive
import com.xechoz.sharefile.resources.action_share
import com.xechoz.sharefile.resources.cd_open_save_folder
import com.xechoz.sharefile.resources.exit_receive_confirm
import com.xechoz.sharefile.resources.menu_change_save_folder
import com.xechoz.sharefile.resources.menu_open_folder
import com.xechoz.sharefile.resources.receive_empty_desc
import com.xechoz.sharefile.resources.receive_empty_title
import com.xechoz.sharefile.resources.saved_files_count
import com.xechoz.sharefile.resources.snackbar_command_copied
import com.xechoz.sharefile.resources.snackbar_file_saved
import com.xechoz.sharefile.resources.snackbar_link_copied
import com.xechoz.sharefile.resources.snackbar_no_app_can_open
import com.xechoz.sharefile.resources.snackbar_no_file_manager
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
import com.xechoz.sharefile.ui.components.serverErrorMessage
import com.xechoz.sharefile.ui.icons.AppIcons
import com.xechoz.sharefile.ui.layout.LocalWindowLayout
import com.xechoz.sharefile.ui.layout.WindowLayout
import com.xechoz.sharefile.ui.theme.ShareFileTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
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

    val shareTitle = stringResource(Res.string.action_share)
    val share: (ReceivedFile) -> Unit = { container.platform.shareFile(it, shareTitle) }

    var pendingOpen by remember { mutableStateOf<ReceivedFile?>(null) }
    val open: (ReceivedFile) -> Unit = { file ->
        if (!container.platform.openFile(file)) pendingOpen = file
    }
    val pending = pendingOpen
    val openFailedMessage = pending?.let { stringResource(Res.string.snackbar_no_app_can_open, it.name) }
    LaunchedEffect(pending, openFailedMessage) {
        if (pending == null || openFailedMessage == null) return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = openFailedMessage,
            actionLabel = shareTitle,
        )
        if (result == SnackbarResult.ActionPerformed) share(pending)
        pendingOpen = null
    }

    val noFileManager = stringResource(Res.string.snackbar_no_file_manager)
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
                scope.launch { snackbarHostState.showSnackbar(noFileManager) }
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
    val linkCopied = stringResource(Res.string.snackbar_link_copied)
    val commandCopied = stringResource(Res.string.snackbar_command_copied)
    val shareLabel = stringResource(Res.string.action_share)

    DoubleBackHandler(
        message = stringResource(Res.string.exit_receive_confirm),
        onBack = onBack,
        showMessage = { snackbarHostState.showSnackbar(it) },
    )

    val seen = remember { received.mapTo(mutableSetOf()) { it.savedPath } }
    var saved by remember { mutableStateOf<List<ReceivedFile>?>(null) }
    LaunchedEffect(received) {
        val fresh = received.filter { seen.add(it.savedPath) }
        if (fresh.isNotEmpty()) saved = fresh
    }
    val savedNotice = saved
    val savedMessage = savedNotice?.let {
        if (it.size == 1) {
            stringResource(Res.string.snackbar_file_saved, it.first().name, downloadFolderName)
        } else {
            pluralStringResource(Res.plurals.saved_files_count, it.size, it.size, downloadFolderName)
        }
    }
    val openLabel = stringResource(Res.string.action_open)
    LaunchedEffect(savedNotice, savedMessage) {
        if (savedNotice == null || savedMessage == null) return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = savedMessage,
            actionLabel = if (savedNotice.size == 1) openLabel else null,
        )
        if (result == SnackbarResult.ActionPerformed) onOpen(savedNotice.first())
        saved = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.action_receive)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(AppIcons.ArrowLeft, contentDescription = stringResource(Res.string.action_back))
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
                                onCopied = { scope.launch { snackbarHostState.showSnackbar(linkCopied) } },
                                preferUrl = preferUrl,
                            )
                            FirewallSection(
                                firewallStatus = firewallStatus,
                                isAllowingFirewall = isAllowingFirewall,
                                firewallCommand = firewallCommand,
                                onAllowFirewall = onAllowFirewall,
                                onCopied = { scope.launch { snackbarHostState.showSnackbar(commandCopied) } },
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
                            onCopied = { scope.launch { snackbarHostState.showSnackbar(linkCopied) } },
                            preferUrl = preferUrl,
                        )
                        FirewallSection(
                            firewallStatus = firewallStatus,
                            isAllowingFirewall = isAllowingFirewall,
                            firewallCommand = firewallCommand,
                            onAllowFirewall = onAllowFirewall,
                            onCopied = { scope.launch { snackbarHostState.showSnackbar(commandCopied) } },
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
                icon = AppIcons.OpenInNew,
                label = openLabel,
                onClick = {
                    sheetFile = null
                    onOpen(file)
                },
            )
            FileActionRow(
                icon = AppIcons.Share,
                label = shareLabel,
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
            icon = AppIcons.Download,
            title = stringResource(Res.string.receive_empty_title),
            description = stringResource(Res.string.receive_empty_desc),
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
                        AppIcons.OpenInNew,
                        contentDescription = stringResource(Res.string.action_open),
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
            Icon(AppIcons.FolderOpen, contentDescription = stringResource(Res.string.cd_open_save_folder))
        }
        if (onChooseFolder != null) {
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.menu_open_folder, folderName)) },
                    onClick = {
                        expanded = false
                        onOpenFolder()
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.menu_change_save_folder)) },
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

        is ServerState.Error -> ServerErrorCard(message = serverErrorMessage(serverState.reason), onRetry = onRetry)

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
