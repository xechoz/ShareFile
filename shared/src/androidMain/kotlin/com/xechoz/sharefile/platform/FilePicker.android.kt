package com.xechoz.sharefile.platform

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.xechoz.sharefile.model.SharedFile
import java.util.UUID

@Composable
actual fun rememberFilePicker(onPicked: (List<SharedFile>) -> Unit): () -> Unit {
    val resolver = LocalContext.current.contentResolver
    val currentOnPicked by rememberUpdatedState(onPicked)
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        currentOnPicked(uris.mapNotNull { it.toSharedFile(resolver) })
    }
    return remember(launcher) { { launcher.launch(arrayOf("*/*")) } }
}

private fun Uri.toSharedFile(resolver: ContentResolver): SharedFile? {
    val name = resolver.query(this, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
    } ?: lastPathSegment ?: "file"
    val size = resolver.query(this, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (index >= 0 && cursor.moveToFirst()) cursor.getLong(index) else 0L
    } ?: 0L
    return SharedFile(
        id = UUID.randomUUID().toString(),
        locator = toString(),
        name = name,
        size = size,
    )
}
