package com.xechoz.sharefile.platform

import androidx.activity.ComponentActivity
import com.xechoz.sharefile.net.HttpFileDownloader
import com.xechoz.sharefile.server.NanoHttpdFileServer

object AndroidAppContainer {

    fun create(activity: ComponentActivity): AppContainer {
        val app = activity.applicationContext
        val assets = AndroidAssetProvider(app.assets)
        val downloads = AndroidDownloadStore(app)
        val content = AndroidContentSource(app.contentResolver)
        return AppContainer(
            platform = AndroidPlatformServices(activity),
            firewall = AndroidFirewallService(),
            newFileServer = { NanoHttpdFileServer(assets, downloads, content) },
            newFileDownloader = { HttpFileDownloader(downloads) },
        )
    }
}
