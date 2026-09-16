package com.xechoz.sharefile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
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
import com.xechoz.sharefile.ui.theme.PillShape
import com.xechoz.sharefile.ui.theme.ShareFileTheme

@Composable
fun HomeScreen(
    onShare: () -> Unit,
    onReceive: () -> Unit,
    onScan: () -> Unit,
    onAbout: () -> Unit,
    onFeedback: () -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
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
            Spacer(Modifier.height(48.dp))
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
            Spacer(Modifier.height(24.dp))
            OutlinedButton(
                onClick = onScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = PillShape,
            ) {
                Icon(
                    Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.size(12.dp))
                Text("Scan QR code", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.Center) {
                TextButton(onClick = onAbout) {
                    Text("About me")
                }
                TextButton(onClick = onFeedback) {
                    Text("Feedback")
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
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
        HomeScreen(onShare = {}, onReceive = {}, onScan = {}, onAbout = {}, onFeedback = {})
    }
}
