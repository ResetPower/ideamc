package org.imcl.logs.test

import org.imcl.logs.LogManager
import java.net.InetAddress

fun main(args: Array<String>) {
    val logger = LogManager.getLogger()
    logger.info("*** ideamc 0.5 - test for log module ***")
    logger.warn("It's a test for warning...")
    logger.info("Doing task Pinging baidu.com...")
    val host =  "baidu.com"
    val timeOut =  3000
    val status = InetAddress.getByName(host).isReachable(timeOut)
    logger.info("Ping done, status: $status")
    logger.error("Error occurred! It's time to shut down this application!")
}