package com.xechoz.sharefile.platform

import com.xechoz.sharefile.resources.Res
import com.xechoz.sharefile.resources.app_name
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString

fun appDisplayName(): String = runBlocking { getString(Res.string.app_name) }
