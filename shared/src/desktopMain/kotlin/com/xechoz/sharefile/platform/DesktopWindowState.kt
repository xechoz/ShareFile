package com.xechoz.sharefile.platform

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import java.awt.Dimension
import java.util.prefs.Preferences

object DesktopWindowState {

    val defaultSize: DpSize = DpSize(960.dp, 640.dp)
    val minSize: Dimension = Dimension(480, 560)

    private val prefs: Preferences =
        Preferences.userRoot().node("com/xechoz/sharefile/window")

    fun load(): Pair<DpSize, WindowPosition> {
        val width = prefs.getInt(KEY_WIDTH, 0)
        val height = prefs.getInt(KEY_HEIGHT, 0)
        val size = if (width > 0 && height > 0) DpSize(width.dp, height.dp) else defaultSize

        val x = prefs.getInt(KEY_X, NONE)
        val y = prefs.getInt(KEY_Y, NONE)
        val position = if (x != NONE && y != NONE) {
            WindowPosition(x.dp, y.dp)
        } else {
            WindowPosition(Alignment.Center)
        }
        return size to position
    }

    fun save(state: WindowState) {
        prefs.putInt(KEY_WIDTH, state.size.width.value.toInt())
        prefs.putInt(KEY_HEIGHT, state.size.height.value.toInt())
        (state.position as? WindowPosition.Absolute)?.let { absolute ->
            prefs.putInt(KEY_X, absolute.x.value.toInt())
            prefs.putInt(KEY_Y, absolute.y.value.toInt())
        }
    }

    private const val KEY_WIDTH = "width"
    private const val KEY_HEIGHT = "height"
    private const val KEY_X = "x"
    private const val KEY_Y = "y"
    private const val NONE = Int.MIN_VALUE
}
