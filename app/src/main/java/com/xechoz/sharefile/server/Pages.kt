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
        """<li><a href="/download/${f.id}">${escapeHtml(f.name)}</a>
           <span class="size">${formatSize(f.size)}</span></li>"""
    }
    return """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>ShareFile - Download</title>
<style>
  body { font-family: system-ui, sans-serif; margin: 0; padding: 24px;
         background: #f5f5f7; color: #1c1c1e; }
  .card { max-width: 480px; margin: 40px auto; background: #fff;
          border-radius: 16px; padding: 32px; box-shadow: 0 2px 16px rgba(0,0,0,.08); }
  h1 { font-size: 22px; margin: 0 0 16px; }
  ul { list-style: none; padding: 0; }
  li { display: flex; justify-content: space-between; align-items: center;
       padding: 14px 0; border-bottom: 1px solid #eee; }
  a { color: #1565C0; text-decoration: none; font-weight: 500; }
  .size { color: #8e8e93; font-size: 13px; }
</style>
</head>
<body>
  <div class="card">
    <h1>Available files</h1>
    <ul>$items</ul>
  </div>
</body>
</html>
""".trimIndent()
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
