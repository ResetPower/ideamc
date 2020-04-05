package org.imcl.lang

import org.imcl.toolkit.MyProperties
import java.io.File

class Translator(val languageName: String) {
    val prop = MyProperties(File("lang/$languageName.properties"))

    fun get(key: String) = prop.get(key)
}