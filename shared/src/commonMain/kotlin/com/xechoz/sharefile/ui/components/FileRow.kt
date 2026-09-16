package com.xechoz.sharefile.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xechoz.sharefile.platform.rememberFileImage
import com.xechoz.sharefile.server.formatSize

private val ThumbSize = 48.dp
private val ThumbShape = RoundedCornerShape(8.dp)

@Composable
fun FileRow(
    name: String,
    size: Long,
    modifier: Modifier = Modifier,
    locator: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.size(12.dp))
        } else {
            FileThumb(name = name, locator = locator)
            Spacer(Modifier.size(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = formatSize(size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (trailing != null) {
            Spacer(Modifier.size(8.dp))
            trailing()
        }
    }
}

@Composable
private fun FileThumb(name: String, locator: String?) {
    if (locator != null && isImage(name)) {
        val bitmap = rememberFileImage(locator)
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(ThumbSize)
                    .clip(ThumbShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            return
        }
    }
    FileEmojiBadge(name)
}

@Composable
private fun FileEmojiBadge(name: String) {
    Box(
        modifier = Modifier.size(ThumbSize),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = fileEmoji(name), fontSize = 28.sp)
    }
}
