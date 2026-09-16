package com.xechoz.sharefile.ui.components

fun fileEmoji(name: String): String {
    val ext = name.substringAfterLast('.', "").lowercase()
    return when (ext) {
        "jpg", "jpeg", "png", "gif", "webp", "heic", "bmp", "svg" -> "🖼️"
        "mp4", "mov", "mkv", "avi", "webm" -> "🎬"
        "mp3", "wav", "flac", "aac", "ogg", "m4a" -> "🎵"
        "zip", "rar", "7z", "tar", "gz" -> "📦"
        "pdf" -> "📕"
        "doc", "docx" -> "📘"
        "xls", "xlsx", "csv" -> "📗"
        "ppt", "pptx" -> "📙"
        "apk" -> "🤖"
        "txt", "md", "log" -> "📄"
        else -> "📄"
    }
}
