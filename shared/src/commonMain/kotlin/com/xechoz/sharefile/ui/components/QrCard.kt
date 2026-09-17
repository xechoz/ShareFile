package com.xechoz.sharefile.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xechoz.sharefile.platform.qrCodeMatrix
import com.xechoz.sharefile.ui.theme.ShareFileTheme
import kotlinx.coroutines.delay

private val QrSize = 140.dp
private const val QR_MODULES = 128

@Composable
fun QrCard(
    url: String,
    onCopied: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(CopiedFeedbackMillis)
            copied = false
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClickLabel = "Copy link") {
                clipboard.setText(AnnotatedString(url))
                copied = true
                onCopied()
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            QrImage(url = url, size = QrSize)
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Scan with the other device's camera",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            CopyLinkGroup(url = url, copied = copied)
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Same Wi-Fi required",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun QrImage(
    url: String,
    size: Dp = QrSize,
    modifier: Modifier = Modifier,
) {
    val matrix = remember(url) { qrCodeMatrix(url, QR_MODULES) }
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = Color.White,
        shadowElevation = 1.dp,
    ) {
        Canvas(
            modifier = Modifier
                .padding(8.dp)
                .size(size),
        ) {
            val cell = this.size.minDimension / matrix.size
            matrix.forEachIndexed { y, row ->
                row.forEachIndexed { x, dark ->
                    if (dark) {
                        drawRect(
                            color = Color.Black,
                            topLeft = Offset(x * cell, y * cell),
                            size = Size(cell, cell),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
private fun QrCardPreview() {
    ShareFileTheme {
        QrCard(
            url = "http://192.168.1.42:8080",
            onCopied = {},
        )
    }
}
