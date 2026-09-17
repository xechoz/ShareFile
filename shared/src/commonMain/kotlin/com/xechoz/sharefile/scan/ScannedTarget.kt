package com.xechoz.sharefile.scan

import com.xechoz.sharefile.server.ShareRoutes

sealed interface ScannedTarget {
    data class Share(val url: String) : ScannedTarget
    data class Browser(val url: String) : ScannedTarget
    data object Unrecognized : ScannedTarget
}

fun classifyScanned(raw: String): ScannedTarget {
    val trimmed = raw.trim()
    val lower = trimmed.lowercase()
    val isHttp = lower.startsWith("http://") || lower.startsWith("https://")
    if (!isHttp) return ScannedTarget.Unrecognized

    val path = trimmed
        .substringAfter("://")
        .substringAfter('/', missingDelimiterValue = "")
        .substringBefore('?')
        .substringBefore('#')

    return if ("/$path" == ShareRoutes.SHARE) {
        ScannedTarget.Share(trimmed)
    } else {
        ScannedTarget.Browser(trimmed)
    }
}
