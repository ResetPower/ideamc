package org.imcl.struct

import java.util.*

class StructedList<T>(val list: Vector<T>, val sep: String) {
    override fun toString(): String {
        val sb = StringBuffer()
        for (i in list) {
            sb.append(i)
            sb.append(",")
        }
        return sb.removeSuffix(",").toString()
    }
}