package com.xechoz.sharefile.ui.scan

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xechoz.sharefile.platform.LocalAppContainer
import com.xechoz.sharefile.platform.PlatformQrScanner
import com.xechoz.sharefile.scan.ScannedTarget
import com.xechoz.sharefile.scan.classifyScanned
import com.xechoz.sharefile.ui.icons.AppIcons
import com.xechoz.sharefile.ui.theme.ShareFileTheme
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay

private const val MESSAGE_DURATION_MS = 2_500L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    onBack: () -> Unit,
    onShareUrl: (String) -> Unit,
    onBrowserUrl: (String) -> Unit,
) {
    val container = LocalAppContainer.current
    ScanContent(
        supported = container.platform.qrScanSupported,
        onBack = onBack,
        onShareUrl = onShareUrl,
        onBrowserUrl = onBrowserUrl,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScanContent(
    supported: Boolean,
    onBack: () -> Unit,
    onShareUrl: (String) -> Unit,
    onBrowserUrl: (String) -> Unit,
) {
    var message by remember { mutableStateOf<String?>(null) }
    var resetToken by remember { mutableStateOf(0) }

    LaunchedEffect(message) {
        if (message != null) {
            delay(MESSAGE_DURATION_MS)
            message = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan QR code") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(AppIcons.ArrowLeft, contentDescription = "Back")
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
                PlatformQrScanner(
                    onResult = { raw ->
                        when (val target = classifyScanned(raw)) {
                            is ScannedTarget.Share -> onShareUrl(target.url)
                            is ScannedTarget.Browser -> onBrowserUrl(target.url)
                            ScannedTarget.Unrecognized -> {
                                message = "Unrecognized QR code"
                                resetToken++
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    resetToken = resetToken,
                )
            } else {
                Text(
                    text = "QR scanning is not available on this platform",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            message?.let {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.inverseSurface,
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ScanScreenPreview() {
    ShareFileTheme {
        ScanContent(supported = true, onBack = {}, onShareUrl = {}, onBrowserUrl = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ScanScreenUnsupportedPreview() {
    ShareFileTheme {
        ScanContent(supported = false, onBack = {}, onShareUrl = {}, onBrowserUrl = {})
    }
}
