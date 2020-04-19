package org.imcl.core.download

import org.apache.commons.io.FileUtils
import java.io.File
import java.net.URL

object DownloadManager {
    @JvmStatic
    fun download(url: String, file: File) {
        try {
            val dir = File(file.path.removeSuffix(file.name))
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val httpUrl = URL(url)
            FileUtils.copyURLToFile(httpUrl, file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}