package org.imcl.constraints

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.imcl.core.constraints.LoggerInit

const val VERSION_NAME = "Alpha 0.4.0"

const val VERSION_CODE = 6

lateinit var logger: Logger

const val WINDOW_HEIGHT = 502.0

const val WINDOW_WIDTH = 840.0

object LoggerInit {
    fun initLogger() {
        logger = LogManager.getLogger(LoggerInit.javaClass)
    }
}
