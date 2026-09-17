package com.xechoz.sharefile.net

import com.xechoz.sharefile.model.RemoteFile

sealed interface RemoteError {
    data object LoadFailed : RemoteError
    data object DownloadFailed : RemoteError
    data class Server(val code: Int) : RemoteError
}

class RemoteException(val code: Int) : Exception("Server returned $code")

interface FileDownloader {

    suspend fun fetchFiles(shareUrl: String): List<RemoteFile>

    suspend fun download(shareUrl: String, file: RemoteFile): String
}
