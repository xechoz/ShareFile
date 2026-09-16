(function () {
  var form = document.getElementById('upload-form');
  var input = document.getElementById('file-input');
  var picker = document.getElementById('picker');
  var list = document.getElementById('file-list');
  var listHeader = document.getElementById('list-header');
  var summary = document.getElementById('summary');
  var empty = document.getElementById('empty');
  var uploadBtn = document.getElementById('upload-btn');
  var pickBtn = document.getElementById('pick-btn');
  var clearAll = document.getElementById('clear-all');
  var toast = document.getElementById('toast');
  var toastText = document.getElementById('toast-text');
  var toastAction = document.getElementById('toast-action');

  if (!form || !input || !picker) return;

  input.classList.add('visually-hidden');
  picker.hidden = false;

  var entries = [];
  var uploading = false;
  var toastTimer = null;
  var undo = null;

  function keyOf(file) {
    return file.name + '|' + file.size + '|' + file.lastModified;
  }

  function formatSize(bytes) {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
    return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB';
  }

  function emojiOf(name) {
    var ext = name.indexOf('.') >= 0 ? name.split('.').pop().toLowerCase() : '';
    if (['jpg', 'jpeg', 'png', 'gif', 'webp', 'heic', 'bmp', 'svg'].indexOf(ext) >= 0) return '🖼️';
    if (['mp4', 'mov', 'mkv', 'avi', 'webm'].indexOf(ext) >= 0) return '🎬';
    if (['mp3', 'wav', 'flac', 'aac', 'ogg', 'm4a'].indexOf(ext) >= 0) return '🎵';
    if (['zip', 'rar', '7z', 'tar', 'gz'].indexOf(ext) >= 0) return '📦';
    if (ext === 'pdf') return '📕';
    if (['doc', 'docx'].indexOf(ext) >= 0) return '📘';
    if (['xls', 'xlsx', 'csv'].indexOf(ext) >= 0) return '📗';
    if (['ppt', 'pptx'].indexOf(ext) >= 0) return '📙';
    if (ext === 'apk') return '🤖';
    return '📄';
  }

  function isImage(name) {
    var ext = name.indexOf('.') >= 0 ? name.split('.').pop().toLowerCase() : '';
    return ['jpg', 'jpeg', 'png', 'gif', 'webp', 'heic', 'bmp', 'svg'].indexOf(ext) >= 0;
  }

  function thumbFor(file) {
    if (isImage(file.name) && window.URL && URL.createObjectURL) {
      var img = document.createElement('img');
      img.className = 'thumb';
      img.alt = '';
      img.src = URL.createObjectURL(file);
      img.addEventListener('load', function () { URL.revokeObjectURL(img.src); });
      img.addEventListener('error', function () {
        URL.revokeObjectURL(img.src);
        img.replaceWith(iconFor(file.name));
      });
      return img;
    }
    return iconFor(file.name);
  }

  function iconFor(name) {
    var span = document.createElement('span');
    span.className = 'icon';
    span.textContent = emojiOf(name);
    return span;
  }

  function syncRow(entry) {
    var el = entry.el;
    if (!el) return;
    var busy = entry.status === 'uploading';
    el.progress.classList.toggle('show', busy);
    el.fill.style.width = entry.progress + '%';
    el.size.textContent = busy
      ? formatSize(entry.file.size) + ' · ' + entry.progress + '%'
      : formatSize(entry.file.size);
    el.status.textContent = entry.status === 'done' ? '✅'
      : entry.status === 'error' ? '❌' : '';
    el.li.classList.toggle('error', entry.status === 'error');
  }

  function rowFor(entry, index) {
    var li = document.createElement('li');
    li.className = 'file-row';

    li.appendChild(thumbFor(entry.file));

    var meta = document.createElement('span');
    meta.className = 'meta';
    var name = document.createElement('span');
    name.className = 'name';
    name.textContent = entry.file.name;
    var size = document.createElement('span');
    size.className = 'size';
    var progress = document.createElement('span');
    progress.className = 'progress';
    var fill = document.createElement('span');
    fill.className = 'progress-fill';
    progress.appendChild(fill);
    meta.appendChild(name);
    meta.appendChild(size);
    meta.appendChild(progress);
    li.appendChild(meta);

    var status = document.createElement('span');
    status.className = 'status';
    li.appendChild(status);

    var remove = document.createElement('button');
    remove.className = 'remove';
    remove.type = 'button';
    remove.textContent = '✕';
    remove.setAttribute('aria-label', 'Remove ' + entry.file.name);
    remove.addEventListener('click', function () { removeAt(index); });
    li.appendChild(remove);

    entry.el = { li: li, size: size, progress: progress, fill: fill, status: status, remove: remove };
    syncRow(entry);
    return li;
  }

  function render() {
    list.textContent = '';
    entries.forEach(function (entry, index) {
      list.appendChild(rowFor(entry, index));
    });
    updateChrome();
  }

  function updateChrome() {
    var hasFiles = entries.length > 0;
    var pending = entries.filter(function (entry) { return entry.status !== 'done'; }).length;
    var uploaded = entries.length - pending;

    listHeader.hidden = !hasFiles;
    empty.hidden = hasFiles;
    pickBtn.textContent = hasFiles ? 'Add more files' : 'Select files';
    pickBtn.classList.toggle('is-disabled', uploading);
    input.disabled = uploading;
    clearAll.disabled = uploading;
    uploadBtn.disabled = uploading || pending === 0;
    if (!uploading) uploadBtn.textContent = 'Upload';

    entries.forEach(function (entry) {
      if (entry.el) entry.el.remove.disabled = uploading;
    });

    if (hasFiles) {
      var total = entries.reduce(function (sum, entry) { return sum + entry.file.size; }, 0);
      var label = entries.length + (entries.length === 1 ? ' file · ' : ' files · ') + formatSize(total);
      if (uploaded > 0) label += ' · ' + uploaded + ' uploaded';
      summary.textContent = label;
    }
  }

  function showToast(message, actionLabel, onAction) {
    toastText.textContent = message;
    toastAction.hidden = !actionLabel;
    toastAction.textContent = actionLabel || '';
    undo = onAction || null;
    toast.classList.add('show');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(hideToast, actionLabel ? 4000 : 1500);
  }

  function hideToast() {
    toast.classList.remove('show');
    undo = null;
  }

  toastAction.addEventListener('click', function () {
    var action = undo;
    hideToast();
    if (action) action();
  });

  function addFiles(picked) {
    var seen = {};
    entries.forEach(function (entry) { seen[keyOf(entry.file)] = true; });
    var added = 0;
    Array.prototype.forEach.call(picked, function (file) {
      var key = keyOf(file);
      if (seen[key]) return;
      seen[key] = true;
      entries.push({ file: file, status: 'pending', progress: 0, el: null });
      added++;
    });
    if (added > 0) render();
  }

  function removeAt(index) {
    if (uploading) return;
    var removed = entries[index];
    entries.splice(index, 1);
    render();
    showToast('Removed ' + removed.file.name, 'Undo', function () {
      entries.splice(index, 0, removed);
      render();
    });
  }

  function uploadOne(entry, onDone) {
    entry.status = 'uploading';
    entry.progress = 0;
    syncRow(entry);

    var data = new FormData();
    data.append('file', entry.file, entry.file.name);

    var xhr = new XMLHttpRequest();
    xhr.open('POST', '/upload');
    xhr.upload.addEventListener('progress', function (event) {
      if (!event.lengthComputable) return;
      entry.progress = Math.round((event.loaded / event.total) * 100);
      syncRow(entry);
    });
    xhr.addEventListener('load', function () {
      var ok = xhr.status >= 200 && xhr.status < 300;
      entry.status = ok ? 'done' : 'error';
      entry.progress = ok ? 100 : entry.progress;
      syncRow(entry);
      onDone(ok, entry.file.name);
    });
    xhr.addEventListener('error', function () {
      entry.status = 'error';
      syncRow(entry);
      onDone(false, entry.file.name);
    });
    xhr.send(data);
  }

  function startUpload(queue) {
    uploading = true;
    updateChrome();
    var total = queue.length;
    var index = 0;
    var failed = 0;

    function next() {
      if (index >= total) {
        uploading = false;
        render();
        var uploaded = total - failed;
        if (failed === 0) {
          showToast('Uploaded ' + total + (total === 1 ? ' file' : ' files'));
        } else if (uploaded === 0) {
          showToast('Upload failed');
        } else {
          showToast(uploaded + ' uploaded · ' + failed + ' failed');
        }
        return;
      }
      var entry = queue[index];
      index++;
      uploadBtn.textContent = 'Uploading ' + index + '/' + total + '…';
      uploadOne(entry, function (ok) {
        if (!ok) failed++;
        next();
      });
    }

    next();
  }

  input.addEventListener('change', function () {
    addFiles(input.files);
    input.value = '';
  });

  clearAll.addEventListener('click', function () {
    if (uploading || entries.length === 0) return;
    var removed = entries;
    entries = [];
    render();
    showToast('Cleared all files', 'Undo', function () {
      entries = removed;
      render();
    });
  });

  form.addEventListener('submit', function (event) {
    event.preventDefault();
    if (uploading) return;
    var queue = entries.filter(function (entry) { return entry.status !== 'done'; });
    if (queue.length > 0) startUpload(queue);
  });

  render();
})();
