package com.xechoz.sharefile.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.xechoz.sharefile.resources.Res
import com.xechoz.sharefile.resources.file_count_size
import com.xechoz.sharefile.server.formatSize
import org.jetbrains.compose.resources.pluralStringResource

@Composable
fun FileListHeader(
    count: Int,
    totalSize: Long,
    modifier: Modifier = Modifier,
) {
    Text(
        text = pluralStringResource(Res.plurals.file_count_size, count, count, formatSize(totalSize)),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth(),
    )
}
