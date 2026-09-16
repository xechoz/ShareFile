package com.xechoz.sharefile.platform

import com.xechoz.sharefile.net.FileDownloader
import com.xechoz.sharefile.server.FileServer

class AppContainer(
    val platform: PlatformServices,
    val newFileServer: () -> FileServer,
    val newFileDownloader: () -> FileDownloader,
)
