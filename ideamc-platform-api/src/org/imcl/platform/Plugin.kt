package org.imcl.platform

import org.apache.logging.log4j.LogManager

open class Plugin {
    protected val logger = LogManager.getLogger()
    lateinit var info: PluginInfo
    open fun onLoad() {
        logger.info("Loading Plugin ${info.name}")
    }
}