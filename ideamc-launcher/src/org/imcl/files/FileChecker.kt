package org.imcl.files

import org.imcl.constraints.logger
import java.io.File

object FileChecker {
    @JvmStatic
    fun check() {
        logger.info("Checking ideamc.json")
        val ideamcJson = File("ideamc.json")
        if (!ideamcJson.exists()) {
            logger.info("ideamc.json not exists, creating file")
            ideamcJson.writeText("""
                {
                    "accounts": {
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
            logger.info("ideamc.json exists")
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