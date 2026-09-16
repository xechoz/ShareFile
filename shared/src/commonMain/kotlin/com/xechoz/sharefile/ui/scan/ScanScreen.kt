package com.xechoz.sharefile.ui.scan

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xechoz.sharefile.platform.LocalAppContainer
import com.xechoz.sharefile.platform.PlatformQrScanner
import com.xechoz.sharefile.ui.theme.ShareFileTheme
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    onBack: () -> Unit,
    onResult: (String) -> Unit,
) {
    val container = LocalAppContainer.current
    ScanContent(
        supported = container.platform.qrScanSupported,
        onBack = onBack,
        onResult = onResult,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScanContent(
    supported: Boolean,
    onBack: () -> Unit,
    onResult: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan QR code") },
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
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (supported) {
                PlatformQrScanner(onResult = onResult, modifier = Modifier.fillMaxSize())
            } else {
                Text(
                    text = "QR scanning is not available on this platform",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ScanScreenPreview() {
    ShareFileTheme {
        ScanContent(supported = true, onBack = {}, onResult = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ScanScreenUnsupportedPreview() {
    ShareFileTheme {
        ScanContent(supported = false, onBack = {}, onResult = {})
    }
}
