package org.imcl.core.log

import java.text.SimpleDateFormat
import java.util.*

object Log {
    fun i(x: String) {
        println("[${SimpleDateFormat("kk:mm:ss").format(Date())}] [IMCL Core/INFO] $x")
    }
    fun e(x: String) {
        System.err.println("[${SimpleDateFormat("kk:mm:ss").format(Date())}] [IMCL Core/ERROR] $x")
    }
    fun w(x: String) {
        println("[${SimpleDateFormat("kk:mm:ss").format(Date())}] [IMCL Core/WARN] $x")
    }
}