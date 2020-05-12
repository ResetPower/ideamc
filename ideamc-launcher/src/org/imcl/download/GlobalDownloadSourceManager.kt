package org.imcl.download

import org.imcl.constraints.Toolkit
import org.imcl.constraints.logger

object GlobalDownloadSourceManager {
    var downloadSrc = getFromConfig()
    @JvmStatic
    fun saveToConfig(str: String) {
        logger.info("Saving download source to config: $str")
        downloadSrc = str
        Toolkit.obj.getJSONObject("settings").put("downloadSrc", str)
        Toolkit.save()
    }
    @JvmStatic
    fun getFromConfig() : String {
        val properties = Toolkit.obj.getJSONObject("settings")
        if (!properties.containsKey("downloadSrc")) {
            saveToConfig("bmclapi")
            return "bmclapi"
        }
        val ret = properties.getString("downloadSrc")
        logger.info("Getting download source from config: $ret")
        return ret
    }
}