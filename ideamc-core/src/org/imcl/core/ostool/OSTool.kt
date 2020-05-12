package org.imcl.core.ostool

import org.imcl.core.constraints.logger

object OSTool {
    @JvmStatic
    fun getOS() : OS {
        val osString = System.getProperty("os.name")
        logger.info("Getting os from system, os.name=$osString")
        return if (osString.toLowerCase().indexOf("mac")!=-1&&osString.toLowerCase().indexOf("os")!=-1) {
            logger.info("Done. OS is macOS")
            OS.MacOS
        } else if (osString.toLowerCase().indexOf("windows")!=-1&&osString.indexOf("10")!=-1) {
            logger.info("Done. OS is Windows10")
            OS.Windows10
        } else if (osString.toLowerCase().indexOf("windows")!=-1) {
            logger.info("Done. OS is Windows")
            OS.Windows
        } else if (osString.toLowerCase().indexOf("linux")!=-1) {
            logger.info("Done. OS is Linux")
            OS.Linux
        } else {
            logger.info("Done. OS is Unknown")
            OS.Unknown
        }
    }
}