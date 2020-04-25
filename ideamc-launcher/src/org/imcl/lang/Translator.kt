package org.imcl.lang

import org.imcl.toolkit.MyProperties
import java.io.BufferedReader
import java.io.InputStreamReader

class Translator(val languageName: String) {
    val prop = MyProperties(BufferedReader(InputStreamReader(javaClass.getResourceAsStream("/org/imcl/lang/$languageName.lang"))))

    fun get(key: String) = prop.get(key)
}