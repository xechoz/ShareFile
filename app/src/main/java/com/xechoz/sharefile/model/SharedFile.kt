package com.xechoz.sharefile.model

import android.net.Uri

data class SharedFile(
    val id: String,
    val uri: Uri,
    val name: String,
    val size: Long,
)
