package com.xechoz.sharefile.server

import android.content.res.AssetManager
import com.xechoz.sharefile.model.ReceivedFile
import com.xechoz.sharefile.model.SharedFile
import com.xechoz.sharefile.ui.components.fileEmoji
import com.xechoz.sharefile.ui.components.isImage

internal class Pages(private val assets: AssetManager) {

    private val templates = mutableMapOf<String, String>()

    fun uploadPage(): String = template("upload.html")

    fun successPage(saved: List<ReceivedFile>): String =
        template("success.html").replace(ITEMS, successItems(saved))

    fun sharePage(files: List<SharedFile>): String =
        template("share.html").replace(ITEMS, shareItems(files))

    private fun template(name: String): String =
        templates.getOrPut(name) {
            assets.open("web/$name").bufferedReader().use { it.readText() }
        }

    private fun successItems(saved: List<ReceivedFile>): String =
        saved.joinToString("") { "<li>${escapeHtml(it.name)} (${formatSize(it.size)})</li>" }

    private fun shareItems(files: List<SharedFile>): String =
        files.joinToString("") { f ->
            val icon = if (isImage(f.name)) {
                """<img class="thumb" src="/thumb/${f.id}" alt="" loading="lazy"
                     data-emoji="${fileEmoji(f.name)}">"""
            } else {
                """<span class="icon">${fileEmoji(f.name)}</span>"""
            }
            """<li><a class="row" href="/download/${f.id}">
                 $icon
                 <span class="meta">
                   <span class="name">${escapeHtml(f.name)}</span>
                   <span class="size">${formatSize(f.size)}</span>
                 </span>
                 <span class="dl">⬇</span>
               </a></li>"""
        }

    private companion object {
        const val ITEMS = "{{items}}"
    }
}

private fun escapeHtml(value: String): String =
    value.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

internal fun formatSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
    bytes < 1024L * 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024))
    else -> "%.2f GB".format(bytes / (1024.0 * 1024 * 1024))
}
