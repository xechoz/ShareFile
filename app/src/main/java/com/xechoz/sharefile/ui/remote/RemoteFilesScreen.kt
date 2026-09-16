package com.xechoz.sharefile.ui.remote

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xechoz.sharefile.ui.components.FileRow
import com.xechoz.sharefile.ui.theme.PillShape
import com.xechoz.sharefile.ui.theme.ShareFileTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteFilesScreen(
    url: String,
    onBack: () -> Unit,
    viewModel: RemoteFilesViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(url) { viewModel.load(url) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Remote files") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            when (val current = state) {
                RemoteUiState.Loading -> Centered {
                    CircularProgressIndicator()
                }

                is RemoteUiState.Loaded -> LoadedContent(
                    state = current,
                    onToggle = viewModel::toggle,
                    onDownload = viewModel::downloadSelected,
                )

                is RemoteUiState.Downloading -> Centered {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Downloading ${current.current}/${current.total}")
                    }
                }

                is RemoteUiState.Done -> Centered {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("Downloaded ${current.count} file(s)")
                    }
                }

                is RemoteUiState.Error -> Centered {
                    Text(
                        text = current.message,
                        color = MaterialTheme.colorScheme.error,
                    )
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.files, key = { it.id }) { file ->
                FileRow(
                    name = file.name,
                    size = file.size,
                    modifier = Modifier.clickable { onToggle(file.id) },
                    leading = {
                        Checkbox(checked = file.id in state.selected, onCheckedChange = { onToggle(file.id) })
                    },
                )
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
        RemoteFilesScreen(url = "http://192.168.1.10:8080/share", onBack = {})
    }
}
