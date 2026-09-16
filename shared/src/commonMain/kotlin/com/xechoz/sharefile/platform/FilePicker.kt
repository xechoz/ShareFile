package com.xechoz.sharefile.platform

import androidx.compose.runtime.Composable
import com.xechoz.sharefile.model.SharedFile

@Composable
expect fun rememberFilePicker(onPicked: (List<SharedFile>) -> Unit): () -> Unit
