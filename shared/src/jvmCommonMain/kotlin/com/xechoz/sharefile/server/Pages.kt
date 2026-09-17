package com.xechoz.sharefile.server

import com.xechoz.sharefile.model.ReceivedFile
import com.xechoz.sharefile.model.SharedFile
import com.xechoz.sharefile.platform.AssetProvider
import com.xechoz.sharefile.ui.components.fileEmoji
import com.xechoz.sharefile.ui.components.isImage

internal class Pages(private val assets: AssetProvider) {

    private val templates = mutableMapOf<String, String>()

    fun uploadPage(strings: WebStrings): String =
        render(template("upload.html"), strings)

    fun successPage(saved: List<ReceivedFile>, strings: WebStrings): String =
        render(template("success.html"), strings).replace(ITEMS, successItems(saved))

    fun sharePage(files: List<SharedFile>, strings: WebStrings): String =
        render(template("share.html"), strings).replace(ITEMS, shareItems(files))

    private fun render(template: String, strings: WebStrings): String {
        var out = template.replace(LANG, strings.lang).replace(DIR, strings.dir)
        strings.htmlTokens().forEach { (key, value) ->
            out = out.replace("{{$key}}", escapeHtml(value))
        }
        return out.replace(I18N, strings.jsJson())
    }

    private fun template(name: String): String =
        templates.getOrPut(name) { assets.text("web/$name").orEmpty() }

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
        const val I18N = "{{i18n}}"
        const val LANG = "{{lang}}"
        const val DIR = "{{dir}}"
    }
}

private fun escapeHtml(value: String): String =
    value.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
