package com.xechoz.sharefile.server

import kotlin.math.round

internal fun formatSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${fixed(bytes / 1024.0, 1)} KB"
    bytes < 1024L * 1024 * 1024 -> "${fixed(bytes / (1024.0 * 1024), 1)} MB"
    else -> "${fixed(bytes / (1024.0 * 1024 * 1024), 2)} GB"
}

private fun fixed(value: Double, decimals: Int): String {
    var factor = 1L
    repeat(decimals) { factor *= 10 }
    val scaled = round(value * factor).toLong()
    val whole = scaled / factor
    val fraction = (scaled % factor).toString().padStart(decimals, '0')
    return "$whole.$fraction"
}
