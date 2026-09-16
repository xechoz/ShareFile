package com.xechoz.sharefile.net

import com.xechoz.sharefile.model.RemoteFile

interface FileDownloader {

    suspend fun fetchFiles(shareUrl: String): List<RemoteFile>

    suspend fun download(shareUrl: String, file: RemoteFile): String
}
