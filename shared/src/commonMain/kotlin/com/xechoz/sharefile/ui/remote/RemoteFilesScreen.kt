package com.xechoz.sharefile.ui.remote

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xechoz.sharefile.model.RemoteFile
import com.xechoz.sharefile.platform.LocalAppContainer
import com.xechoz.sharefile.ui.components.ContentContainer
import com.xechoz.sharefile.ui.components.FileRow
import com.xechoz.sharefile.ui.icons.AppIcons
import com.xechoz.sharefile.ui.theme.PillShape
import com.xechoz.sharefile.ui.theme.ShareFileTheme
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteFilesScreen(
    url: String,
    onBack: () -> Unit,
) {
    val container = LocalAppContainer.current
    val viewModel: RemoteFilesViewModel = viewModel {
        RemoteFilesViewModel(container.newFileDownloader())
    }
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(url) { viewModel.load(url) }

    RemoteFilesContent(
        state = state,
        onBack = onBack,
        onToggle = viewModel::toggle,
        onDownload = viewModel::downloadSelected,
        onOpenInBrowser = { container.platform.openUrl(url) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RemoteFilesContent(
    state: RemoteUiState,
    onBack: () -> Unit,
    onToggle: (String) -> Unit,
    onDownload: () -> Unit,
    onOpenInBrowser: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Remote files") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(AppIcons.ArrowLeft, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        ContentContainer(
            modifier = Modifier.padding(padding),
            maxWidth = 720.dp,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                when (state) {
                    RemoteUiState.Loading -> Centered {
                        CircularProgressIndicator()
                    }

                    is RemoteUiState.Loaded -> LoadedContent(
                        state = state,
                        onToggle = onToggle,
                        onDownload = onDownload,
                    )

                    is RemoteUiState.Downloading -> Centered {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(16.dp))
                            Text("Downloading ${state.current}/${state.total}")
                        }
                    }

                    is RemoteUiState.Done -> Centered {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                AppIcons.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(48.dp),
                            )
                            Spacer(Modifier.height(12.dp))
                            Text("Downloaded ${state.count} file(s)")
                        }
                    }

                    is RemoteUiState.Error -> Centered {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                            )
                            Spacer(Modifier.height(16.dp))
                            OutlinedButton(onClick = onOpenInBrowser, shape = PillShape) {
                                Text("Open in browser")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadedContent(
    state: RemoteUiState.Loaded,
    onToggle: (String) -> Unit,
    onDownload: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (state.files.isEmpty()) {
            Centered { Text("No files available") }
            return@Column
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
        ) {
            itemsIndexed(state.files, key = { _, file -> file.id }) { index, file ->
                FileRow(
                    name = file.name,
                    size = file.size,
                    modifier = Modifier.clickable { onToggle(file.id) },
                    leading = {
                        Checkbox(checked = file.id in state.selected, onCheckedChange = { onToggle(file.id) })
                    },
                )
                if (index < state.files.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 68.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onDownload,
            enabled = state.selected.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            shape = PillShape,
        ) {
            Text("Download selected (${state.selected.size})")
        }
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun RemoteFilesScreenPreview() {
    ShareFileTheme {
        RemoteFilesContent(
            state = RemoteUiState.Loaded(
                files = listOf(
                    RemoteFile(id = "1", name = "photo.jpg", size = 2_400_000),
                    RemoteFile(id = "2", name = "report.pdf", size = 512_000),
                ),
                selected = setOf("1"),
            ),
            onBack = {},
            onToggle = {},
            onDownload = {},
            onOpenInBrowser = {},
        )
    }
}
