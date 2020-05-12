package org.imcl.files

import org.imcl.constraints.logger
import java.io.File

object FileChecker {
    @JvmStatic
    fun check() {
        logger.info("Checking imcl.json")
        val imclJson = File("imcl.json")
        if (!imclJson.exists()) {
            logger.info("imcl.json not exists, creating file")
            imclJson.writeText("""
                {
                    "account": {
                        "username": "none",
                        "uuid": "none",
                        "accessToken": "none"
                    },
                    "profiles": [],
                    "settings": {
                        "isLoggedIn": "false",
                        "language": "english",
                        "javapath": "java",
                        "downloadSrc": "bmclapi"
                    }
                }
            """.trimIndent())
        } else {
            logger.info("imcl.json exists")
        }
        logger.info("Checking plugins folder")
        val pluginsFolder = File("plugins")
        if (!pluginsFolder.exists()) {
            logger.info("plugins folder not exists, creating folder")
            pluginsFolder.mkdirs()
        } else {
            logger.info("plugins folder exists")
        }
    }
}