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

  var files = [];
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

  function rowFor(file, index) {
    var li = document.createElement('li');
    li.className = 'file-row';

    li.appendChild(thumbFor(file));

    var meta = document.createElement('span');
    meta.className = 'meta';
    var name = document.createElement('span');
    name.className = 'name';
    name.textContent = file.name;
    var size = document.createElement('span');
    size.className = 'size';
    size.textContent = formatSize(file.size);
    meta.appendChild(name);
    meta.appendChild(size);
    li.appendChild(meta);

    var remove = document.createElement('button');
    remove.className = 'remove';
    remove.type = 'button';
    remove.textContent = '✕';
    remove.setAttribute('aria-label', 'Remove ' + file.name);
    remove.addEventListener('click', function () { removeAt(index); });
    li.appendChild(remove);

    return li;
  }

  function render() {
    list.textContent = '';
    files.forEach(function (file, index) {
      list.appendChild(rowFor(file, index));
    });

    var hasFiles = files.length > 0;
    listHeader.hidden = !hasFiles;
    empty.hidden = hasFiles;
    uploadBtn.disabled = !hasFiles;
    pickBtn.textContent = hasFiles ? 'Add more files' : 'Select files';

    if (hasFiles) {
      var total = files.reduce(function (sum, file) { return sum + file.size; }, 0);
      summary.textContent = files.length + (files.length === 1 ? ' file · ' : ' files · ') + formatSize(total);
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
    files.forEach(function (file) { seen[keyOf(file)] = true; });
    var added = 0;
    Array.prototype.forEach.call(picked, function (file) {
      var key = keyOf(file);
      if (seen[key]) return;
      seen[key] = true;
      files.push(file);
      added++;
    });
    if (added > 0) render();
  }

  function removeAt(index) {
    var removed = files[index];
    files.splice(index, 1);
    render();
    showToast('Removed ' + removed.name, 'Undo', function () {
      files.splice(index, 0, removed);
      render();
    });
  }

  input.addEventListener('change', function () {
    addFiles(input.files);
    input.value = '';
  });

  clearAll.addEventListener('click', function () {
    if (files.length === 0) return;
    var removed = files;
    files = [];
    render();
    showToast('Cleared all files', 'Undo', function () {
      files = removed;
      render();
    });
  });

  form.addEventListener('submit', function (event) {
    if (files.length === 0) {
      event.preventDefault();
      return;
    }
    if (typeof DataTransfer === 'undefined') return;
    var transfer = new DataTransfer();
    files.forEach(function (file) { transfer.items.add(file); });
    input.files = transfer.files;
  });

  render();
})();
