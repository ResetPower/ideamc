package org.imcl.constraints

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

const val VERSION_NAME = "Alpha 0.2.0"

const val VERSION_CODE = 4

lateinit var logger: Logger

fun initLogger() {
    logger = LogManager.getLogger()
}