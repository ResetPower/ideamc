package org.imcl.core.download

import javafx.application.Platform
import javafx.scene.control.Alert
import org.apache.commons.io.FileUtils
import java.io.File
import java.net.URL

object DownloadManager {
    @JvmStatic
    fun download(url: String, file: File, onError: (e: Exception) -> Unit = {
        Platform.runLater {
            val a = Alert(Alert.AlertType.ERROR)
            a.title = "Error occurred in downloading."
            a.headerText = "Error occurred in downloading."
            a.contentText = it.localizedMessage
            a.show()
        }
    }) {
        try {
            val dir = File(file.path.removeSuffix(file.name))
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val httpUrl = URL(url)
            FileUtils.copyURLToFile(httpUrl, file)
        } catch (e: Exception) {
            onError(e)
        }
    }
}