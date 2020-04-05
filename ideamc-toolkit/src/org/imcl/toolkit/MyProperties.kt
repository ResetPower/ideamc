package org.imcl.toolkit

import java.io.File

class MyProperties(val file: File) {
    val map = HashMap<String, String>()
    init {
        for (i in file.readLines()) {
            if (i.trim()!=""&&(!i.startsWith("#"))) {
                val spl = i.split("=")
                map.put(spl[0], spl[1])
            }
        }
    }
    fun put(key: String, value: String) {
        map.put(key, value)
    }
    fun get(key: String) = map[key]
}