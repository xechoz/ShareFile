package com.xechoz.sharefile.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PlatformQrScanner(onResult: (String) -> Unit, modifier: Modifier = Modifier)
