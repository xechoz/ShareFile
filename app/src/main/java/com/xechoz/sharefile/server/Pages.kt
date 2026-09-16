package com.xechoz.sharefile.server

import com.xechoz.sharefile.model.ReceivedFile
import com.xechoz.sharefile.model.SharedFile

internal fun uploadPage(): String = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>ShareFile - Upload</title>
<style>
  body { font-family: system-ui, sans-serif; margin: 0; padding: 24px;
         background: #f5f5f7; color: #1c1c1e; }
  .card { max-width: 480px; margin: 40px auto; background: #fff;
          border-radius: 16px; padding: 32px; box-shadow: 0 2px 16px rgba(0,0,0,.08); }
  h1 { font-size: 22px; margin: 0 0 8px; }
  p { color: #6b6b70; font-size: 14px; }
  input[type=file] { display: block; width: 100%; margin: 20px 0; }
  button { width: 100%; padding: 14px; font-size: 16px; border: none;
           border-radius: 10px; background: #1565C0; color: #fff; cursor: pointer; }
  button:active { background: #0d47a1; }
</style>
</head>
<body>
  <div class="card">
    <h1>Send files</h1>
    <p>Choose one or more files to upload to the device.</p>
    <form method="post" action="/upload" enctype="multipart/form-data">
      <input type="file" name="file" multiple required>
      <button type="submit">Upload</button>
    </form>
  </div>
</body>
</html>
""".trimIndent()

internal fun successPage(saved: List<ReceivedFile>): String {
    val items = saved.joinToString("") { "<li>${escapeHtml(it.name)} (${formatSize(it.size)})</li>" }
    return """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>ShareFile - Done</title>
<style>
  body { font-family: system-ui, sans-serif; margin: 0; padding: 24px;
         background: #f5f5f7; color: #1c1c1e; }
  .card { max-width: 480px; margin: 40px auto; background: #fff;
          border-radius: 16px; padding: 32px; box-shadow: 0 2px 16px rgba(0,0,0,.08); }
  h1 { font-size: 22px; margin: 0 0 8px; color: #2e7d32; }
  ul { padding-left: 20px; }
  a { display: inline-block; margin-top: 20px; color: #1565C0; text-decoration: none; }
</style>
</head>
<body>
  <div class="card">
    <h1>Upload complete</h1>
    <ul>$items</ul>
    <a href="/">Upload more</a>
  </div>
</body>
</html>
""".trimIndent()
}

internal fun sharePage(files: List<SharedFile>): String {
    val items = files.joinToString("") { f ->
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
    return """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>ShareFile - Download</title>
<style>
  body { font-family: system-ui, sans-serif; margin: 0; padding: 16px;
         background: #f5f5f7; color: #1c1c1e; }
  .card { max-width: 480px; margin: 16px auto; background: #fff;
          border-radius: 16px; padding: 20px; box-shadow: 0 2px 16px rgba(0,0,0,.08); }
  h1 { font-size: 22px; margin: 0 0 4px; }
  .hint { color: #8e8e93; font-size: 13px; margin: 0 0 8px; }
  ul { list-style: none; padding: 0; margin: 0; }
  .row { display: flex; align-items: center; gap: 12px; min-height: 56px;
         padding: 14px 0; border-bottom: 1px solid #eee;
         text-decoration: none; color: inherit; }
  .row:active { background: #f5f5f7; }
  .icon, .thumb { width: 48px; height: 48px; flex-shrink: 0;
                  display: flex; align-items: center; justify-content: center; }
  .icon { font-size: 28px; }
  .thumb { border-radius: 8px; object-fit: cover; background: #eee; }
  .meta { display: flex; flex-direction: column; min-width: 0; flex: 1; }
  .name { color: #1565C0; font-weight: 500;
          overflow-wrap: anywhere; word-break: break-word; }
  .size { color: #8e8e93; font-size: 13px; margin-top: 2px; }
  .dl { flex-shrink: 0; color: #1565C0; font-size: 18px; }
  .toast { position: fixed; left: 50%; bottom: 32px;
           transform: translateX(-50%) translateY(20px);
           background: rgba(0,0,0,.8); color: #fff; padding: 10px 20px;
           border-radius: 20px; font-size: 14px; opacity: 0;
           pointer-events: none; transition: opacity .25s, transform .25s; }
  .toast.show { opacity: 1; transform: translateX(-50%) translateY(0); }
  @media (max-width: 480px) {
    body { padding: 12px; }
    .card { margin: 8px auto; padding: 16px; }
  }
</style>
</head>
<body>
  <div class="card">
    <h1>Available files</h1>
    <p class="hint">Tap a file to download</p>
    <ul>$items</ul>
  </div>
  <div id="toast" class="toast">Downloading…</div>
  <script>
    document.querySelectorAll('.thumb').forEach(function (img) {
      img.addEventListener('error', function () {
        var span = document.createElement('span');
        span.className = 'icon';
        span.textContent = img.dataset.emoji;
        img.replaceWith(span);
      });
    });
    document.querySelectorAll('.row').forEach(function (row) {
      row.addEventListener('click', function () {
        var t = document.getElementById('toast');
        t.classList.add('show');
        clearTimeout(t._timer);
        t._timer = setTimeout(function () { t.classList.remove('show'); }, 1500);
      });
    });
  </script>
</body>
</html>
""".trimIndent()
}

private fun isImage(name: String): Boolean {
    val ext = name.substringAfterLast('.', "").lowercase()
    return ext in setOf("jpg", "jpeg", "png", "gif", "webp", "heic", "bmp", "svg")
}

private fun fileEmoji(name: String): String {
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
