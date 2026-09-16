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
