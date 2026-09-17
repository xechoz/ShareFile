package com.xechoz.sharefile.feedback

enum class FeedbackType {
    Bug,
    Suggestion,
    Other,
}

data class FeedbackDraft(
    val message: String,
    val type: FeedbackType,
    val email: String?,
    val appName: String,
    val appVersion: String,
    val platform: String,
)

object FeedbackUrl {

    const val REPO_URL = "https://github.com/xechoz/quick-file-share"

    private const val LABEL = "feedback"
    private const val TITLE_MAX = 60
    private const val HEX = "0123456789ABCDEF"

    fun issuesNew(draft: FeedbackDraft): String {
        val title = "[${draft.type.label()}] ${titleSummary(draft.message)}"
        val params = listOf(
            "title" to title,
            "body" to body(draft),
            "labels" to LABEL,
        ).joinToString("&") { (key, value) -> "$key=${encode(value)}" }
        return "$REPO_URL/issues/new?$params"
    }

    private fun body(draft: FeedbackDraft): String = buildString {
        append(draft.message.trim())
        append("\n\n---\n")
        append("**Type:** ${draft.type.label()}\n")
        draft.email?.trim()?.takeIf { it.isNotEmpty() }?.let { append("**Contact:** $it\n") }
        append("**App:** ${draft.appName} ${draft.appVersion}\n")
        append("**Platform:** ${draft.platform}")
    }

    private fun titleSummary(message: String): String {
        val firstLine = message.trim().lineSequence().firstOrNull()?.trim().orEmpty()
        return if (firstLine.length <= TITLE_MAX) firstLine else firstLine.take(TITLE_MAX) + "…"
    }

    private fun encode(value: String): String = buildString {
        for (byte in value.encodeToByteArray()) {
            val code = byte.toInt() and 0xFF
            val char = code.toChar()
            when {
                char in 'A'..'Z' || char in 'a'..'z' || char in '0'..'9' -> append(char)
                char == '-' || char == '_' || char == '.' || char == '*' -> append(char)
                char == ' ' -> append('+')
                else -> {
                    append('%')
                    append(HEX[code shr 4])
                    append(HEX[code and 0x0F])
                }
            }
        }
    }

    private fun FeedbackType.label(): String = when (this) {
        FeedbackType.Bug -> "Bug"
        FeedbackType.Suggestion -> "Suggestion"
        FeedbackType.Other -> "Other"
    }
}
