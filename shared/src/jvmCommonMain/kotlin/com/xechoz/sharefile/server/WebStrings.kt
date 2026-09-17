package com.xechoz.sharefile.server

enum class WebLocale(val code: String, val rtl: Boolean = false) {
    EN("en"),
    ZH("zh"),
    ZH_HANT("zh-Hant"),
    ES("es"),
    FR("fr"),
    DE("de"),
    PT("pt"),
    IT("it"),
    RU("ru"),
    JA("ja"),
    KO("ko"),
    AR("ar", rtl = true),
    HI("hi"),
    ID("id"),
}

fun webLocale(acceptLanguage: String?): WebLocale {
    val header = acceptLanguage ?: return WebLocale.EN
    val candidates = header.split(",").mapNotNull { part ->
        val pieces = part.trim().split(";")
        val tag = pieces.firstOrNull()?.trim().orEmpty()
        if (tag.isEmpty() || tag == "*") return@mapNotNull null
        val quality = pieces.drop(1)
            .firstOrNull { it.trim().startsWith("q=") }
            ?.substringAfter("q=")?.trim()?.toDoubleOrNull()
            ?: 1.0
        tag to quality
    }.sortedByDescending { it.second }
    return candidates.asSequence().map { matchLocale(it.first) }.firstOrNull { it != null } ?: WebLocale.EN
}

private fun matchLocale(tag: String): WebLocale? {
    val subtags = tag.split("-")
    return when (subtags.firstOrNull()?.lowercase()) {
        "en" -> WebLocale.EN
        "zh" -> {
            val rest = subtags.drop(1).map { it.lowercase() }
            if (rest.any { it == "hant" || it == "tw" || it == "hk" || it == "mo" }) {
                WebLocale.ZH_HANT
            } else {
                WebLocale.ZH
            }
        }
        "es" -> WebLocale.ES
        "fr" -> WebLocale.FR
        "de" -> WebLocale.DE
        "pt" -> WebLocale.PT
        "it" -> WebLocale.IT
        "ru" -> WebLocale.RU
        "ja" -> WebLocale.JA
        "ko" -> WebLocale.KO
        "ar" -> WebLocale.AR
        "hi" -> WebLocale.HI
        "id" -> WebLocale.ID
        else -> null
    }
}

internal class WebStrings private constructor(
    private val locale: WebLocale,
    private val values: Map<String, String>,
) {

    val lang: String
        get() = locale.code

    val dir: String
        get() = if (locale.rtl) "rtl" else "ltr"

    fun htmlTokens(): Map<String, String> = values

    fun get(key: String): String = values[key] ?: EN_TABLE[key] ?: key

    fun jsJson(): String =
        values.entries.joinToString(",", "{", "}") { (key, value) ->
            "${jsonString(key)}:${jsonString(value)}"
        }

    companion object {
        fun of(locale: WebLocale): WebStrings = WebStrings(locale, table(locale))

        internal fun table(locale: WebLocale): Map<String, String> =
            when (locale) {
                WebLocale.EN -> EN_TABLE
                WebLocale.ZH -> ZH_TABLE
                WebLocale.ZH_HANT -> ZH_HANT_TABLE
                WebLocale.ES -> ES_TABLE
                WebLocale.FR -> FR_TABLE
                WebLocale.DE -> DE_TABLE
                WebLocale.PT -> PT_TABLE
                WebLocale.IT -> IT_TABLE
                WebLocale.RU -> RU_TABLE
                WebLocale.JA -> JA_TABLE
                WebLocale.KO -> KO_TABLE
                WebLocale.AR -> AR_TABLE
                WebLocale.HI -> HI_TABLE
                WebLocale.ID -> ID_TABLE
            }

        private fun jsonString(value: String): String {
            val escaped = buildString {
                value.forEach { c ->
                    when (c) {
                        '\\' -> append("\\\\")
                        '"' -> append("\\\"")
                        '\n' -> append("\\n")
                        '\r' -> append("\\r")
                        '\t' -> append("\\t")
                        else -> append(c)
                    }
                }
            }
            return "\"$escaped\""
        }
    }
}

private val EN_TABLE = mapOf(
    "upload.title" to "Quick File Share - Upload",
    "upload.heading" to "Send files",
    "upload.lead" to "Choose one or more files to upload to the device.",
    "upload.upload" to "Upload",
    "upload.clear_all" to "Clear all",
    "upload.empty_title" to "No files yet",
    "upload.empty_text" to "Select files, then upload them to the device.",
    "upload.select_files" to "Select files",
    "upload.add_more" to "Add more files",
    "upload.undo" to "Undo",
    "upload.uploading" to "Uploading {current}/{total}…",
    "upload.uploaded_one" to "Uploaded {count} file",
    "upload.uploaded_many" to "Uploaded {count} files",
    "upload.failed" to "Upload failed",
    "upload.partial" to "{uploaded} uploaded · {failed} failed",
    "upload.summary_one" to "{count} file · {size}",
    "upload.summary_many" to "{count} files · {size}",
    "upload.summary_uploaded" to "{count} uploaded",
    "upload.removed" to "Removed {name}",
    "upload.cleared" to "Cleared all files",
    "upload.aria_remove" to "Remove {name}",
    "share.title" to "Quick File Share - Download",
    "share.heading" to "Available files",
    "share.hint" to "Tap a file to download",
    "share.downloading" to "Downloading…",
    "success.title" to "Quick File Share - Done",
    "success.heading" to "Upload complete",
    "success.upload_more" to "Upload more",
)

private val ZH_TABLE = mapOf(
    "upload.title" to "快速文件共享 - 上传",
    "upload.heading" to "发送文件",
    "upload.lead" to "选择一个或多个文件上传到该设备。",
    "upload.upload" to "上传",
    "upload.clear_all" to "全部清除",
    "upload.empty_title" to "暂无文件",
    "upload.empty_text" to "选择文件，然后上传到该设备。",
    "upload.select_files" to "选择文件",
    "upload.add_more" to "添加更多文件",
    "upload.undo" to "撤销",
    "upload.uploading" to "正在上传 {current}/{total}…",
    "upload.uploaded_one" to "已上传 {count} 个文件",
    "upload.uploaded_many" to "已上传 {count} 个文件",
    "upload.failed" to "上传失败",
    "upload.partial" to "已上传 {uploaded} 个 · 失败 {failed} 个",
    "upload.summary_one" to "{count} 个文件 · {size}",
    "upload.summary_many" to "{count} 个文件 · {size}",
    "upload.summary_uploaded" to "已上传 {count} 个",
    "upload.removed" to "已移除 {name}",
    "upload.cleared" to "已清除所有文件",
    "upload.aria_remove" to "移除 {name}",
    "share.title" to "快速文件共享 - 下载",
    "share.heading" to "可下载的文件",
    "share.hint" to "点击文件即可下载",
    "share.downloading" to "正在下载…",
    "success.title" to "快速文件共享 - 完成",
    "success.heading" to "上传完成",
    "success.upload_more" to "继续上传",
)

private val ZH_HANT_TABLE = mapOf(
    "upload.title" to "快速檔案分享 - 上傳",
    "upload.heading" to "傳送檔案",
    "upload.lead" to "選擇一或多個要上傳到裝置的檔案。",
    "upload.upload" to "上傳",
    "upload.clear_all" to "全部清除",
    "upload.empty_title" to "尚無檔案",
    "upload.empty_text" to "選擇檔案，然後將它們上傳到裝置。",
    "upload.select_files" to "選擇檔案",
    "upload.add_more" to "新增更多檔案",
    "upload.undo" to "復原",
    "upload.uploading" to "正在上傳 {current}/{total}…",
    "upload.uploaded_one" to "已上傳 {count} 個檔案",
    "upload.uploaded_many" to "已上傳 {count} 個檔案",
    "upload.failed" to "上傳失敗",
    "upload.partial" to "已上傳 {uploaded} 個 · 失敗 {failed} 個",
    "upload.summary_one" to "{count} 個檔案 · {size}",
    "upload.summary_many" to "{count} 個檔案 · {size}",
    "upload.summary_uploaded" to "已上傳 {count} 個",
    "upload.removed" to "已移除 {name}",
    "upload.cleared" to "已清除所有檔案",
    "upload.aria_remove" to "移除 {name}",
    "share.title" to "快速檔案分享 - 下載",
    "share.heading" to "可下載的檔案",
    "share.hint" to "點選檔案即可下載",
    "share.downloading" to "正在下載…",
    "success.title" to "快速檔案分享 - 完成",
    "success.heading" to "上傳完成",
    "success.upload_more" to "繼續上傳",
)

private val ES_TABLE = mapOf(
    "upload.title" to "Compartir archivos - Subir",
    "upload.heading" to "Enviar archivos",
    "upload.lead" to "Elige uno o más archivos para subirlos al dispositivo.",
    "upload.upload" to "Subir",
    "upload.clear_all" to "Quitar todo",
    "upload.empty_title" to "Aún no hay archivos",
    "upload.empty_text" to "Selecciona archivos y súbelos al dispositivo.",
    "upload.select_files" to "Seleccionar archivos",
    "upload.add_more" to "Añadir más archivos",
    "upload.undo" to "Deshacer",
    "upload.uploading" to "Subiendo {current}/{total}…",
    "upload.uploaded_one" to "Se subió {count} archivo",
    "upload.uploaded_many" to "Se subieron {count} archivos",
    "upload.failed" to "Error al subir",
    "upload.partial" to "{uploaded} subidos · {failed} con error",
    "upload.summary_one" to "{count} archivo · {size}",
    "upload.summary_many" to "{count} archivos · {size}",
    "upload.summary_uploaded" to "{count} subidos",
    "upload.removed" to "Se quitó {name}",
    "upload.cleared" to "Se quitaron todos los archivos",
    "upload.aria_remove" to "Quitar {name}",
    "share.title" to "Compartir archivos - Descargar",
    "share.heading" to "Archivos disponibles",
    "share.hint" to "Toca un archivo para descargarlo",
    "share.downloading" to "Descargando…",
    "success.title" to "Compartir archivos - Listo",
    "success.heading" to "Subida completada",
    "success.upload_more" to "Subir más",
)

private val FR_TABLE = mapOf(
    "upload.title" to "Partage de fichiers - Envoi",
    "upload.heading" to "Envoyer des fichiers",
    "upload.lead" to "Choisissez un ou plusieurs fichiers à envoyer vers l'appareil.",
    "upload.upload" to "Envoyer",
    "upload.clear_all" to "Tout effacer",
    "upload.empty_title" to "Aucun fichier pour l'instant",
    "upload.empty_text" to "Sélectionnez des fichiers, puis envoyez-les vers l'appareil.",
    "upload.select_files" to "Sélectionner des fichiers",
    "upload.add_more" to "Ajouter des fichiers",
    "upload.undo" to "Annuler",
    "upload.uploading" to "Envoi {current}/{total}…",
    "upload.uploaded_one" to "{count} fichier envoyé",
    "upload.uploaded_many" to "{count} fichiers envoyés",
    "upload.failed" to "Échec de l'envoi",
    "upload.partial" to "{uploaded} envoyés · {failed} en échec",
    "upload.summary_one" to "{count} fichier · {size}",
    "upload.summary_many" to "{count} fichiers · {size}",
    "upload.summary_uploaded" to "{count} envoyés",
    "upload.removed" to "{name} supprimé",
    "upload.cleared" to "Tous les fichiers ont été effacés",
    "upload.aria_remove" to "Supprimer {name}",
    "share.title" to "Partage de fichiers - Téléchargement",
    "share.heading" to "Fichiers disponibles",
    "share.hint" to "Appuyez sur un fichier pour le télécharger",
    "share.downloading" to "Téléchargement…",
    "success.title" to "Partage de fichiers - Terminé",
    "success.heading" to "Envoi terminé",
    "success.upload_more" to "Envoyer d'autres fichiers",
)

private val DE_TABLE = mapOf(
    "upload.title" to "Dateien teilen - Hochladen",
    "upload.heading" to "Dateien senden",
    "upload.lead" to "Wähle eine oder mehrere Dateien zum Hochladen auf das Gerät aus.",
    "upload.upload" to "Hochladen",
    "upload.clear_all" to "Alle entfernen",
    "upload.empty_title" to "Noch keine Dateien",
    "upload.empty_text" to "Wähle Dateien aus und lade sie auf das Gerät hoch.",
    "upload.select_files" to "Dateien auswählen",
    "upload.add_more" to "Weitere Dateien hinzufügen",
    "upload.undo" to "Rückgängig",
    "upload.uploading" to "Wird hochgeladen: {current}/{total}…",
    "upload.uploaded_one" to "{count} Datei hochgeladen",
    "upload.uploaded_many" to "{count} Dateien hochgeladen",
    "upload.failed" to "Hochladen fehlgeschlagen",
    "upload.partial" to "{uploaded} hochgeladen · {failed} fehlgeschlagen",
    "upload.summary_one" to "{count} Datei · {size}",
    "upload.summary_many" to "{count} Dateien · {size}",
    "upload.summary_uploaded" to "{count} hochgeladen",
    "upload.removed" to "{name} entfernt",
    "upload.cleared" to "Alle Dateien entfernt",
    "upload.aria_remove" to "{name} entfernen",
    "share.title" to "Dateien teilen - Herunterladen",
    "share.heading" to "Verfügbare Dateien",
    "share.hint" to "Tippe auf eine Datei, um sie herunterzuladen",
    "share.downloading" to "Wird heruntergeladen…",
    "success.title" to "Dateien teilen - Fertig",
    "success.heading" to "Upload abgeschlossen",
    "success.upload_more" to "Weitere Dateien hochladen",
)

private val PT_TABLE = mapOf(
    "upload.title" to "Compartilhar arquivos - Enviar",
    "upload.heading" to "Enviar arquivos",
    "upload.lead" to "Escolha um ou mais arquivos para enviar ao dispositivo.",
    "upload.upload" to "Enviar",
    "upload.clear_all" to "Limpar tudo",
    "upload.empty_title" to "Ainda não há arquivos",
    "upload.empty_text" to "Selecione arquivos e envie-os ao dispositivo.",
    "upload.select_files" to "Selecionar arquivos",
    "upload.add_more" to "Adicionar mais arquivos",
    "upload.undo" to "Desfazer",
    "upload.uploading" to "Enviando {current}/{total}…",
    "upload.uploaded_one" to "{count} arquivo enviado",
    "upload.uploaded_many" to "{count} arquivos enviados",
    "upload.failed" to "Falha no envio",
    "upload.partial" to "{uploaded} enviados · {failed} com falha",
    "upload.summary_one" to "{count} arquivo · {size}",
    "upload.summary_many" to "{count} arquivos · {size}",
    "upload.summary_uploaded" to "{count} enviados",
    "upload.removed" to "{name} removido",
    "upload.cleared" to "Todos os arquivos foram limpos",
    "upload.aria_remove" to "Remover {name}",
    "share.title" to "Compartilhar arquivos - Baixar",
    "share.heading" to "Arquivos disponíveis",
    "share.hint" to "Toque em um arquivo para baixar",
    "share.downloading" to "Baixando…",
    "success.title" to "Compartilhar arquivos - Concluído",
    "success.heading" to "Envio concluído",
    "success.upload_more" to "Enviar mais",
)

private val IT_TABLE = mapOf(
    "upload.title" to "Condividi file - Caricamento",
    "upload.heading" to "Invia file",
    "upload.lead" to "Scegli uno o più file da caricare sul dispositivo.",
    "upload.upload" to "Carica",
    "upload.clear_all" to "Cancella tutto",
    "upload.empty_title" to "Nessun file ancora",
    "upload.empty_text" to "Seleziona i file e caricali sul dispositivo.",
    "upload.select_files" to "Seleziona file",
    "upload.add_more" to "Aggiungi altri file",
    "upload.undo" to "Annulla",
    "upload.uploading" to "Caricamento {current}/{total}…",
    "upload.uploaded_one" to "{count} file caricato",
    "upload.uploaded_many" to "{count} file caricati",
    "upload.failed" to "Caricamento non riuscito",
    "upload.partial" to "{uploaded} caricati · {failed} non riusciti",
    "upload.summary_one" to "{count} file · {size}",
    "upload.summary_many" to "{count} file · {size}",
    "upload.summary_uploaded" to "{count} caricati",
    "upload.removed" to "{name} rimosso",
    "upload.cleared" to "Tutti i file cancellati",
    "upload.aria_remove" to "Rimuovi {name}",
    "share.title" to "Condividi file - Download",
    "share.heading" to "File disponibili",
    "share.hint" to "Tocca un file per scaricarlo",
    "share.downloading" to "Download in corso…",
    "success.title" to "Condividi file - Completato",
    "success.heading" to "Caricamento completato",
    "success.upload_more" to "Carica altri file",
)

private val RU_TABLE = mapOf(
    "upload.title" to "Обмен файлами - Отправка",
    "upload.heading" to "Отправка файлов",
    "upload.lead" to "Выберите один или несколько файлов для отправки на устройство.",
    "upload.upload" to "Отправить",
    "upload.clear_all" to "Очистить всё",
    "upload.empty_title" to "Пока нет файлов",
    "upload.empty_text" to "Выберите файлы и отправьте их на устройство.",
    "upload.select_files" to "Выбрать файлы",
    "upload.add_more" to "Добавить ещё файлы",
    "upload.undo" to "Отменить",
    "upload.uploading" to "Отправка {current}/{total}…",
    "upload.uploaded_one" to "Отправлен {count} файл",
    "upload.uploaded_many" to "Отправлено {count} файлов",
    "upload.failed" to "Не удалось отправить",
    "upload.partial" to "Отправлено {uploaded} · ошибок {failed}",
    "upload.summary_one" to "{count} файл · {size}",
    "upload.summary_many" to "{count} файлов · {size}",
    "upload.summary_uploaded" to "Отправлено {count}",
    "upload.removed" to "{name} удалён",
    "upload.cleared" to "Все файлы очищены",
    "upload.aria_remove" to "Удалить {name}",
    "share.title" to "Обмен файлами - Скачивание",
    "share.heading" to "Доступные файлы",
    "share.hint" to "Нажмите на файл, чтобы скачать",
    "share.downloading" to "Загрузка…",
    "success.title" to "Обмен файлами - Готово",
    "success.heading" to "Отправка завершена",
    "success.upload_more" to "Отправить ещё",
)

private val JA_TABLE = mapOf(
    "upload.title" to "ファイル共有 - アップロード",
    "upload.heading" to "ファイルを送信",
    "upload.lead" to "端末にアップロードするファイルを1つ以上選択してください。",
    "upload.upload" to "アップロード",
    "upload.clear_all" to "すべて消去",
    "upload.empty_title" to "まだファイルがありません",
    "upload.empty_text" to "ファイルを選択して端末にアップロードしてください。",
    "upload.select_files" to "ファイルを選択",
    "upload.add_more" to "ファイルを追加",
    "upload.undo" to "元に戻す",
    "upload.uploading" to "アップロード中 {current}/{total}…",
    "upload.uploaded_one" to "{count} 個のファイルをアップロードしました",
    "upload.uploaded_many" to "{count} 個のファイルをアップロードしました",
    "upload.failed" to "アップロードに失敗しました",
    "upload.partial" to "{uploaded} 件完了 · {failed} 件失敗",
    "upload.summary_one" to "{count} 個のファイル · {size}",
    "upload.summary_many" to "{count} 個のファイル · {size}",
    "upload.summary_uploaded" to "{count} 件アップロード済み",
    "upload.removed" to "{name} を削除しました",
    "upload.cleared" to "すべてのファイルを消去しました",
    "upload.aria_remove" to "{name} を削除",
    "share.title" to "ファイル共有 - ダウンロード",
    "share.heading" to "ダウンロードできるファイル",
    "share.hint" to "ファイルをタップしてダウンロード",
    "share.downloading" to "ダウンロード中…",
    "success.title" to "ファイル共有 - 完了",
    "success.heading" to "アップロード完了",
    "success.upload_more" to "さらにアップロード",
)

private val KO_TABLE = mapOf(
    "upload.title" to "파일 공유 - 업로드",
    "upload.heading" to "파일 보내기",
    "upload.lead" to "기기로 업로드할 파일을 하나 이상 선택하세요.",
    "upload.upload" to "업로드",
    "upload.clear_all" to "모두 지우기",
    "upload.empty_title" to "아직 파일이 없습니다",
    "upload.empty_text" to "파일을 선택한 뒤 기기로 업로드하세요.",
    "upload.select_files" to "파일 선택",
    "upload.add_more" to "파일 더 추가",
    "upload.undo" to "실행 취소",
    "upload.uploading" to "업로드 중 {current}/{total}…",
    "upload.uploaded_one" to "{count}개 파일을 업로드했습니다",
    "upload.uploaded_many" to "{count}개 파일을 업로드했습니다",
    "upload.failed" to "업로드 실패",
    "upload.partial" to "{uploaded}개 완료 · {failed}개 실패",
    "upload.summary_one" to "{count}개 파일 · {size}",
    "upload.summary_many" to "{count}개 파일 · {size}",
    "upload.summary_uploaded" to "{count}개 업로드됨",
    "upload.removed" to "{name} 삭제됨",
    "upload.cleared" to "모든 파일을 지웠습니다",
    "upload.aria_remove" to "{name} 삭제",
    "share.title" to "파일 공유 - 다운로드",
    "share.heading" to "받을 수 있는 파일",
    "share.hint" to "파일을 탭하여 다운로드",
    "share.downloading" to "다운로드 중…",
    "success.title" to "파일 공유 - 완료",
    "success.heading" to "업로드 완료",
    "success.upload_more" to "더 업로드",
)

private val AR_TABLE = mapOf(
    "upload.title" to "مشاركة الملفات - رفع",
    "upload.heading" to "إرسال الملفات",
    "upload.lead" to "اختر ملفًا واحدًا أو أكثر لرفعه إلى الجهاز.",
    "upload.upload" to "رفع",
    "upload.clear_all" to "مسح الكل",
    "upload.empty_title" to "لا توجد ملفات بعد",
    "upload.empty_text" to "اختر الملفات ثم ارفعها إلى الجهاز.",
    "upload.select_files" to "اختيار الملفات",
    "upload.add_more" to "إضافة المزيد من الملفات",
    "upload.undo" to "تراجع",
    "upload.uploading" to "جارٍ الرفع {current}/{total}…",
    "upload.uploaded_one" to "تم رفع ملف واحد",
    "upload.uploaded_many" to "تم رفع {count} ملفًا",
    "upload.failed" to "فشل الرفع",
    "upload.partial" to "تم رفع {uploaded} · فشل {failed}",
    "upload.summary_one" to "ملف واحد · {size}",
    "upload.summary_many" to "{count} ملفات · {size}",
    "upload.summary_uploaded" to "تم رفع {count}",
    "upload.removed" to "تمت إزالة {name}",
    "upload.cleared" to "تم مسح جميع الملفات",
    "upload.aria_remove" to "إزالة {name}",
    "share.title" to "مشاركة الملفات - تنزيل",
    "share.heading" to "الملفات المتاحة",
    "share.hint" to "اضغط على ملف لتنزيله",
    "share.downloading" to "جارٍ التنزيل…",
    "success.title" to "مشاركة الملفات - تم",
    "success.heading" to "اكتمل الرفع",
    "success.upload_more" to "رفع المزيد",
)

private val HI_TABLE = mapOf(
    "upload.title" to "फ़ाइल साझा करें - अपलोड",
    "upload.heading" to "फ़ाइलें भेजें",
    "upload.lead" to "डिवाइस पर अपलोड करने के लिए एक या अधिक फ़ाइलें चुनें।",
    "upload.upload" to "अपलोड करें",
    "upload.clear_all" to "सभी हटाएँ",
    "upload.empty_title" to "अभी कोई फ़ाइल नहीं",
    "upload.empty_text" to "फ़ाइलें चुनें, फिर उन्हें डिवाइस पर अपलोड करें।",
    "upload.select_files" to "फ़ाइलें चुनें",
    "upload.add_more" to "और फ़ाइलें जोड़ें",
    "upload.undo" to "पूर्ववत करें",
    "upload.uploading" to "अपलोड हो रहा है {current}/{total}…",
    "upload.uploaded_one" to "{count} फ़ाइल अपलोड हुई",
    "upload.uploaded_many" to "{count} फ़ाइलें अपलोड हुईं",
    "upload.failed" to "अपलोड विफल",
    "upload.partial" to "{uploaded} अपलोड · {failed} विफल",
    "upload.summary_one" to "{count} फ़ाइल · {size}",
    "upload.summary_many" to "{count} फ़ाइलें · {size}",
    "upload.summary_uploaded" to "{count} अपलोड",
    "upload.removed" to "{name} हटाया गया",
    "upload.cleared" to "सभी फ़ाइलें हटाई गईं",
    "upload.aria_remove" to "{name} हटाएँ",
    "share.title" to "फ़ाइल साझा करें - डाउनलोड",
    "share.heading" to "उपलब्ध फ़ाइलें",
    "share.hint" to "डाउनलोड करने के लिए फ़ाइल पर टैप करें",
    "share.downloading" to "डाउनलोड हो रहा है…",
    "success.title" to "फ़ाइल साझा करें - पूर्ण",
    "success.heading" to "अपलोड पूर्ण",
    "success.upload_more" to "और अपलोड करें",
)

private val ID_TABLE = mapOf(
    "upload.title" to "Berbagi File - Unggah",
    "upload.heading" to "Kirim file",
    "upload.lead" to "Pilih satu atau beberapa file untuk diunggah ke perangkat.",
    "upload.upload" to "Unggah",
    "upload.clear_all" to "Hapus semua",
    "upload.empty_title" to "Belum ada file",
    "upload.empty_text" to "Pilih file, lalu unggah ke perangkat.",
    "upload.select_files" to "Pilih file",
    "upload.add_more" to "Tambah file lain",
    "upload.undo" to "Urungkan",
    "upload.uploading" to "Mengunggah {current}/{total}…",
    "upload.uploaded_one" to "{count} file diunggah",
    "upload.uploaded_many" to "{count} file diunggah",
    "upload.failed" to "Gagal mengunggah",
    "upload.partial" to "{uploaded} diunggah · {failed} gagal",
    "upload.summary_one" to "{count} file · {size}",
    "upload.summary_many" to "{count} file · {size}",
    "upload.summary_uploaded" to "{count} diunggah",
    "upload.removed" to "{name} dihapus",
    "upload.cleared" to "Semua file dihapus",
    "upload.aria_remove" to "Hapus {name}",
    "share.title" to "Berbagi File - Unduh",
    "share.heading" to "File yang tersedia",
    "share.hint" to "Ketuk file untuk mengunduh",
    "share.downloading" to "Mengunduh…",
    "success.title" to "Berbagi File - Selesai",
    "success.heading" to "Unggahan selesai",
    "success.upload_more" to "Unggah lagi",
)
