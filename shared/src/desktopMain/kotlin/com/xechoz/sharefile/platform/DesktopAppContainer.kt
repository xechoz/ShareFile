package com.xechoz.sharefile.platform

import com.xechoz.sharefile.net.HttpFileDownloader
import com.xechoz.sharefile.server.NanoHttpdFileServer

object DesktopAppContainer {

    fun create(): AppContainer {
        val assets = DesktopAssetProvider()
        val downloads = DesktopDownloadStore()
        val content = DesktopContentSource()
        return AppContainer(
            platform = DesktopPlatformServices(),
            newFileServer = { NanoHttpdFileServer(assets, downloads, content) },
            newFileDownloader = { HttpFileDownloader(downloads) },
        )
    }
}
