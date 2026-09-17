package com.xechoz.sharefile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.xechoz.sharefile.ui.layout.LocalWindowLayout
import com.xechoz.sharefile.ui.layout.WindowLayout
import com.xechoz.sharefile.ui.theme.PillShape
import com.xechoz.sharefile.ui.theme.ShareFileTheme

@Composable
fun HomeScreen(
    onShare: () -> Unit,
    onReceive: () -> Unit,
    onScan: () -> Unit,
    onConnectUrl: () -> Unit,
    onAbout: () -> Unit,
    onFeedback: () -> Unit,
    showScan: Boolean = true,
) {
    Scaffold { padding ->
        when (LocalWindowLayout.current) {
            WindowLayout.Expanded -> ExpandedHome(
                onShare = onShare,
                onReceive = onReceive,
                onScan = onScan,
                onConnectUrl = onConnectUrl,
                onAbout = onAbout,
                onFeedback = onFeedback,
                showScan = showScan,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )

            WindowLayout.Compact -> CompactHome(
                onShare = onShare,
                onReceive = onReceive,
                onScan = onScan,
                onConnectUrl = onConnectUrl,
                onAbout = onAbout,
                onFeedback = onFeedback,
                showScan = showScan,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        }
    }
}

@Composable
private fun ExpandedHome(
    onShare: () -> Unit,
    onReceive: () -> Unit,
    onScan: () -> Unit,
    onConnectUrl: () -> Unit,
    onAbout: () -> Unit,
    onFeedback: () -> Unit,
    showScan: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(48.dp),
    ) {
        Column(modifier = Modifier.weight(1.2f)) {
            Text(
                text = "ShareFile",
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Share and receive files over your local network",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 380.dp),
        ) {
            ActionButton(
                label = "Share",
                icon = Icons.Default.Upload,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = onShare,
            )
            Spacer(Modifier.height(16.dp))
            ActionButton(
                label = "Receive",
                icon = Icons.Default.Download,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                onClick = onReceive,
            )
            Spacer(Modifier.height(16.dp))
            SecondaryAction(
                showScan = showScan,
                onScan = onScan,
                onConnectUrl = onConnectUrl,
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AboutButtons(onAbout = onAbout, onFeedback = onFeedback)
            }
        }
    }
}

@Composable
private fun CompactHome(
    onShare: () -> Unit,
    onReceive: () -> Unit,
    onScan: () -> Unit,
    onConnectUrl: () -> Unit,
    onAbout: () -> Unit,
    onFeedback: () -> Unit,
    showScan: Boolean,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val isLandscape = maxWidth > maxHeight
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isLandscape) 24.dp else 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "ShareFile",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Share and receive files over your local network",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(if (isLandscape) 16.dp else 48.dp))
                if (isLandscape) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        ActionButton(
                            label = "Share",
                            icon = Icons.Default.Upload,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            onClick = onShare,
                            modifier = Modifier.weight(1f),
                        )
                        ActionButton(
                            label = "Receive",
                            icon = Icons.Default.Download,
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary,
                            onClick = onReceive,
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    ActionButton(
                        label = "Share",
                        icon = Icons.Default.Upload,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        onClick = onShare,
                    )
                    Spacer(Modifier.height(16.dp))
                    ActionButton(
                        label = "Receive",
                        icon = Icons.Default.Download,
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        onClick = onReceive,
                    )
                }
                Spacer(Modifier.height(if (isLandscape) 16.dp else 24.dp))
                SecondaryAction(
                    showScan = showScan,
                    onScan = onScan,
                    onConnectUrl = onConnectUrl,
                )
            }
            Row(horizontalArrangement = Arrangement.Center) {
                AboutButtons(onAbout = onAbout, onFeedback = onFeedback)
            }
        }
    }
}

@Composable
private fun SecondaryAction(
    showScan: Boolean,
    onScan: () -> Unit,
    onConnectUrl: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = if (showScan) onScan else onConnectUrl,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = PillShape,
    ) {
        Icon(
            if (showScan) Icons.Default.QrCodeScanner else Icons.Default.Link,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.size(12.dp))
        Text(
            text = if (showScan) "Scan QR code" else "Open link",
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun AboutButtons(
    onAbout: () -> Unit,
    onFeedback: () -> Unit,
) {
    TextButton(
        onClick = onAbout,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        Text("About me")
    }
    TextButton(
        onClick = onFeedback,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        Text("Feedback")
    }
}

@Composable
private fun ActionButton(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = PillShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(Modifier.size(12.dp))
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    ShareFileTheme {
        CompactHome(
            onShare = {},
            onReceive = {},
            onScan = {},
            onConnectUrl = {},
            onAbout = {},
            onFeedback = {},
            showScan = true,
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 360)
@Composable
private fun HomeScreenLandscapePreview() {
    ShareFileTheme {
        CompactHome(
            onShare = {},
            onReceive = {},
            onScan = {},
            onConnectUrl = {},
            onAbout = {},
            onFeedback = {},
            showScan = true,
        )
    }
}

@Preview(showBackground = true, widthDp = 1000, heightDp = 640)
@Composable
private fun HomeScreenExpandedPreview() {
    ShareFileTheme {
        ExpandedHome(
            onShare = {},
            onReceive = {},
            onScan = {},
            onConnectUrl = {},
            onAbout = {},
            onFeedback = {},
            showScan = false,
        )
    }
}
