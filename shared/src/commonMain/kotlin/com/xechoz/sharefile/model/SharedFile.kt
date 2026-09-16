package com.xechoz.sharefile.model

data class SharedFile(
    val id: String,
    val locator: String,
    val name: String,
    val size: Long,
)
