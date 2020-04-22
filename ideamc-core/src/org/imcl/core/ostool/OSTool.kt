package org.imcl.core.ostool

object OSTool {
    @JvmStatic
    fun getOS() : OS {
        val osString = System.getProperty("os.name")
        return if (osString.toLowerCase().indexOf("mac")!=-1&&osString.toLowerCase().indexOf("os")!=-1) {
            OS.MacOS
        } else if (osString.toLowerCase().indexOf("windows")!=-1&&osString.indexOf("10")!=-1) {
            OS.Windows10
        } else if (osString.toLowerCase().indexOf("windows")!=-1) {
            OS.Windows
        } else if (osString.toLowerCase().indexOf("linux")!=-1) {
            OS.Linux
        } else {
            OS.Unknown
        }
    }
}