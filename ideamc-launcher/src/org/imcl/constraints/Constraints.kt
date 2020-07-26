package org.imcl.constraints

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

const val VERSION_NAME = "Alpha 0.3.0"

const val VERSION_CODE = 5

lateinit var logger: Logger

const val WINDOW_HEIGHT = 502.0

const val WINDOW_WIDTH = 840.0

fun initLogger() {
    logger = LogManager.getLogger()
}