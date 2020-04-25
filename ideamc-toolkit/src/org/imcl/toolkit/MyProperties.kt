package org.imcl.toolkit

import java.io.Reader

class MyProperties(private val reader: Reader) {
    val map = HashMap<String, String>()
    init {
        for (i in reader.readLines()) {
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