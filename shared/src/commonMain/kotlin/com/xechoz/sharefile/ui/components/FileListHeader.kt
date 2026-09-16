package com.xechoz.sharefile.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.xechoz.sharefile.server.formatSize

@Composable
fun FileListHeader(
    count: Int,
    totalSize: Long,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "$count ${if (count == 1) "file" else "files"} · ${formatSize(totalSize)}",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth(),
    )
}
