package com.xechoz.sharefile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
    onAbout: () -> Unit,
    onFeedback: () -> Unit,
    showAboutButtons: Boolean,
    showScan: Boolean = true,
) {
    Scaffold { padding ->
        when (LocalWindowLayout.current) {
            WindowLayout.Expanded -> ExpandedHome(
                onShare = onShare,
                onReceive = onReceive,
                onScan = onScan,
                onAbout = onAbout,
                onFeedback = onFeedback,
                showAboutButtons = showAboutButtons,
                showScan = showScan,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )

            WindowLayout.Compact -> CompactHome(
                onShare = onShare,
                onReceive = onReceive,
                onScan = onScan,
                onAbout = onAbout,
                onFeedback = onFeedback,
                showAboutButtons = showAboutButtons,
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
    onAbout: () -> Unit,
    onFeedback: () -> Unit,
    showAboutButtons: Boolean,
    showScan: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .widthIn(max = 560.dp)
                .padding(horizontal = 32.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Icon(
                    Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .padding(20.dp)
                        .size(36.dp),
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = "ShareFile",
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Share and receive files over your local network",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "The other device just opens the link in a browser — no install needed",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ActionTile(
                    title = "Share",
                    description = "Send files from this device",
                    icon = Icons.Default.Upload,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                )
                ActionTile(
                    title = "Receive",
                    description = "Get files from another device",
                    icon = Icons.Default.Download,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = onReceive,
                    modifier = Modifier.weight(1f),
                )
            }
            if (showScan) {
                Spacer(Modifier.height(16.dp))
                ScanAction(
                    onScan = onScan,
                    height = 48.dp,
                    shape = MaterialTheme.shapes.small,
                )
            }
            if (showAboutButtons) {
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    AboutButtons(onAbout = onAbout, onFeedback = onFeedback)
                }
            }
        }
    }
}

@Composable
private fun ActionTile(
    title: String,
    description: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(140.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun CompactHome(
    onShare: () -> Unit,
    onReceive: () -> Unit,
    onScan: () -> Unit,
    onAbout: () -> Unit,
    onFeedback: () -> Unit,
    showAboutButtons: Boolean,
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
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
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
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        onClick = onReceive,
                    )
                }
                if (showScan) {
                    Spacer(Modifier.height(if (isLandscape) 16.dp else 24.dp))
                    ScanAction(onScan = onScan)
                }
            }
            if (showAboutButtons) {
                Row(horizontalArrangement = Arrangement.Center) {
                    AboutButtons(onAbout = onAbout, onFeedback = onFeedback)
                }
            }
        }
    }
}

@Composable
private fun ScanAction(
    onScan: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 56.dp,
    shape: Shape = PillShape,
) {
    OutlinedButton(
        onClick = onScan,
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        shape = shape,
    ) {
        Icon(
            Icons.Default.QrCodeScanner,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.size(12.dp))
        Text(
            text = "Scan QR code",
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
        Text("About")
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
            onAbout = {},
            onFeedback = {},
            showAboutButtons = true,
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
            onAbout = {},
            onFeedback = {},
            showAboutButtons = true,
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
            onAbout = {},
            onFeedback = {},
            showAboutButtons = false,
            showScan = false,
        )
    }
}

@Preview(showBackground = true, widthDp = 720, heightDp = 560)
@Composable
private fun HomeScreenExpandedMinSizePreview() {
    ShareFileTheme {
        ExpandedHome(
            onShare = {},
            onReceive = {},
            onScan = {},
            onAbout = {},
            onFeedback = {},
            showAboutButtons = true,
            showScan = false,
        )
    }
}
