package org.imcl.logs.files

import java.io.File

object FileChecker {
    @JvmStatic
    fun check() {
        val fold = File("logs")
        if (!fold.exists()) {
            fold.mkdir()
        }
        val latest = File("logs/latest.log")
        if (latest.exists()) {
            latest.writeText("")
        }
    }
    @JvmStatic
    fun newLog(text: String) {
        val latest = File("logs/latest.log")
        latest.appendText("$text\n")
    }
}
