package com.xechoz.sharefile.platform

object DesktopUiScale {

    fun resolve(env: (String) -> String?, prop: (String) -> String?): Float {
        val override = prop(UI_SCALE_PROPERTY) ?: env(UI_SCALE_ENV)
        val raw = override ?: env("GDK_SCALE")
        return (raw?.toFloatOrNull() ?: 1f).coerceIn(1f, 3f)
    }

    fun apply() {
        if (!isLinux()) return
        System.setProperty("skiko.linux.autodpi", "false")
        val scale = resolve(System::getenv, System::getProperty)
        if (scale > 1f) {
            System.setProperty("sun.java2d.uiScale.enabled", "true")
            System.setProperty("sun.java2d.uiScale", scale.toString())
        }
    }

    fun systemFontScale(run: (List<String>) -> String? = ::runCommand): Float {
        System.getProperty(FONT_SCALE_PROPERTY)?.toFloatOrNull()?.let { return it.clampFontScale() }
        val raw = when {
            isLinux() -> run(listOf("gsettings", "get", "org.gnome.desktop.interface", "text-scaling-factor"))
            isWindows() -> run(listOf("reg", "query", "HKCU\\Software\\Microsoft\\Accessibility", "/v", "TextScaleFactor"))
            else -> null
        } ?: return 1f
        return if (isWindows()) parseRegistryFontScale(raw) else parseGsettingsFontScale(raw)
    }

    fun parseGsettingsFontScale(raw: String): Float =
        raw.trim().trim('\'').toFloatOrNull()?.clampFontScale() ?: 1f

    fun parseRegistryFontScale(raw: String): Float {
        val token = raw.lineSequence()
            .map(String::trim)
            .firstOrNull { it.contains("TextScaleFactor", ignoreCase = true) }
            ?.split(Regex("\\s+"))
            ?.lastOrNull()
            ?: return 1f
        val value = if (token.startsWith("0x", ignoreCase = true)) {
            token.substring(2).toIntOrNull(16)
        } else {
            token.toIntOrNull()
        } ?: return 1f
        return (value / 100f).clampFontScale()
    }

    private fun Float.clampFontScale(): Float = coerceIn(0.5f, 3f)

    private fun isLinux(): Boolean = System.getProperty("os.name").lowercase().contains("linux")

    private fun isWindows(): Boolean = System.getProperty("os.name").lowercase().contains("windows")

    private fun runCommand(command: List<String>): String? = runCatching {
        ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()
            .inputStream
            .bufferedReader()
            .use { it.readText() }
            .trim()
    }.getOrNull()

    const val UI_SCALE_PROPERTY = "sharefile.uiScale"
    const val UI_SCALE_ENV = "SHAREFILE_UI_SCALE"
    const val FONT_SCALE_PROPERTY = "sharefile.fontScale"
}
