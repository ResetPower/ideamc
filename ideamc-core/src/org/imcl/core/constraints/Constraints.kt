package org.imcl.core.constraints

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

lateinit var logger: Logger

fun initLogger() {
    logger = LogManager.getLogger()
}