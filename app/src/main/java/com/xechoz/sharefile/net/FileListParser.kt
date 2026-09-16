package com.xechoz.sharefile.net

import com.xechoz.sharefile.model.RemoteFile

object FileListParser {

    private val OBJECT_REGEX = Regex("""\{[^{}]*\}""")
    private val ID_REGEX = Regex(""""id"\s*:\s*"((?:[^"\\]|\\.)*)"""")
    private val NAME_REGEX = Regex(""""name"\s*:\s*"((?:[^"\\]|\\.)*)"""")
    private val SIZE_REGEX = Regex(""""size"\s*:\s*(\d+)""")

    fun parse(json: String): List<RemoteFile> =
        OBJECT_REGEX.findAll(json).mapNotNull { match ->
            val body = match.value
            val id = ID_REGEX.find(body)?.groupValues?.getOrNull(1) ?: return@mapNotNull null
            val name = NAME_REGEX.find(body)?.groupValues?.getOrNull(1) ?: return@mapNotNull null
            val size = SIZE_REGEX.find(body)?.groupValues?.getOrNull(1)?.toLongOrNull() ?: 0L
            RemoteFile(id = id, name = unescape(name), size = size)
        }.toList()

    private fun unescape(value: String): String =
        value.replace("\\\"", "\"").replace("\\\\", "\\")
}
