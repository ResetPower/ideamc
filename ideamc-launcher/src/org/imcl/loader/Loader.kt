package org.imcl.loader

import org.imcl.loader.Constraints.isLoggedIn
import org.imcl.loader.Constraints.language
import java.io.FileInputStream
import java.util.*

object Loader {
    @JvmStatic
    fun readProperties() : Properties {
        val pro = Properties()
        val `in` = FileInputStream("properties/ideamc.properties")
        pro.load(`in`)
        `in`.close()
        language = pro.getProperty("language")
        isLoggedIn = pro.getProperty("isLoggedIn")=="true"
        return pro
    }
}