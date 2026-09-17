/*
 * Icons from Lucide (https://lucide.dev).
 *
 * ISC License
 *
 * Copyright (c) for portions of Lucide are held by Cole Bemis 2013-2022 as part of Feather (MIT).
 * All other copyright (c) for Lucide are held by Lucide Contributors 2022.
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package com.xechoz.sharefile.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

object AppIcons {
    val ArrowLeft: ImageVector by lazy {
        lucide("ArrowLeft", "m12 19-7-7 7-7", "M19 12H5", autoMirror = true)
    }

    val Close: ImageVector by lazy {
        lucide("Close", "M18 6 6 18", "m6 6 12 12")
    }

    val Upload: ImageVector by lazy {
        lucide(
            "Upload",
            "M12 3v12",
            "m17 8-5-5-5 5",
            "M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4",
        )
    }

    val Add: ImageVector by lazy {
        lucide("Add", "M5 12h14", "M12 5v14")
    }

    val OpenInNew: ImageVector by lazy {
        lucide(
            "OpenInNew",
            "M15 3h6v6",
            "M10 14 21 3",
            "M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6",
            autoMirror = true,
        )
    }

    val Share: ImageVector by lazy {
        lucide(
            "Share",
            "M15 5a3 3 0 1 0 6 0a3 3 0 1 0-6 0",
            "M3 12a3 3 0 1 0 6 0a3 3 0 1 0-6 0",
            "M15 19a3 3 0 1 0 6 0a3 3 0 1 0-6 0",
            "M8.59 13.51L15.42 17.49",
            "M15.41 6.51L8.59 10.49",
        )
    }

    val Download: ImageVector by lazy {
        lucide(
            "Download",
            "M12 15V3",
            "M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4",
            "m7 10 5 5 5-5",
        )
    }

    val FolderOpen: ImageVector by lazy {
        lucide(
            "FolderOpen",
            "m6 14 1.5-2.9A2 2 0 0 1 9.24 10H20a2 2 0 0 1 1.94 2.5l-1.54 6a2 2 0 0 1-1.95 1.5H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h3.9a2 2 0 0 1 1.69.9l.81 1.2a2 2 0 0 0 1.67.9H18a2 2 0 0 1 2 2v2",
        )
    }

    val CheckCircle: ImageVector by lazy {
        lucide(
            "CheckCircle",
            "M2 12a10 10 0 1 0 20 0a10 10 0 1 0-20 0",
            "m16 9-5.5 5.5L8 12",
        )
    }

    val ErrorOutline: ImageVector by lazy {
        lucide(
            "ErrorOutline",
            "M2 12a10 10 0 1 0 20 0a10 10 0 1 0-20 0",
            "M12 8v4",
            "M12 16h.01",
        )
    }

    val Warning: ImageVector by lazy {
        lucide(
            "Warning",
            "m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3",
            "M12 9v4",
            "M12 17h.01",
        )
    }

    val ContentCopy: ImageVector by lazy {
        lucide(
            "ContentCopy",
            "M10 8h10a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H10a2 2 0 0 1-2-2V10a2 2 0 0 1 2-2z",
            "M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2",
        )
    }

    val Check: ImageVector by lazy {
        lucide("Check", "M20 6 9 17l-5-5")
    }

    val SwapHoriz: ImageVector by lazy {
        lucide(
            "SwapHoriz",
            "M8 3 4 7l4 4",
            "M4 7h16",
            "m16 21 4-4-4-4",
            "M20 17H4",
        )
    }

    val QrCodeScanner: ImageVector by lazy {
        lucide(
            "QrCodeScanner",
            "M4 3h3a1 1 0 0 1 1 1v3a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1z",
            "M17 3h3a1 1 0 0 1 1 1v3a1 1 0 0 1-1 1h-3a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1z",
            "M4 16h3a1 1 0 0 1 1 1v3a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1v-3a1 1 0 0 1 1-1z",
            "M21 16h-3a2 2 0 0 0-2 2v3",
            "M21 21v.01",
            "M12 7v3a2 2 0 0 1-2 2H7",
            "M3 12h.01",
            "M12 3h.01",
            "M12 16v.01",
            "M16 12h1",
            "M21 12v.01",
            "M12 21v-1",
        )
    }
}

private fun lucide(
    name: String,
    vararg paths: String,
    autoMirror: Boolean = false,
): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
    autoMirror = autoMirror,
).apply {
    paths.forEach { pathData ->
        addPath(
            pathData = PathParser().parsePathString(pathData).toNodes(),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
    }
}.build()
