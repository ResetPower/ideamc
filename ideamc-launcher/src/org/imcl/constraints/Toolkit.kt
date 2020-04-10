package org.imcl.constraints

import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

object Toolkit {
    @JvmStatic
    fun getCurrentLanguage() : String {
        val ins = FileInputStream("imcl/properties/ideamc.properties")
        val prop = Properties()
        prop.load(ins)
        ins.close()
        return prop.getProperty("language")
    }
    @JvmStatic
    fun updateLanguage(language: String) {
        val ins = FileInputStream("imcl/properties/ideamc.properties")
        val prop = Properties()
        prop.load(ins)
        ins.close()
        prop.setProperty("language", language)
        val out = FileOutputStream("imcl/properties/ideamc.properties")
        prop.store(out, "")
        out.close()
    }
    @JvmStatic
    fun getLanguageEnglishName(language: String) : String {
        return when (language) {
            "简体中文" -> "chinesesimplified"
            "繁體中文" -> "chinesetraditional"
            "English" -> "english"
            "Esperanto" -> "esperanto"
            "日本語" -> "japanese"
            else -> "unknown"
        }
    }
    @JvmStatic
    fun getLanguageNameInThatLanguage(language: String) : String {
        return when (language) {
            "chinesesimplified" -> "简体中文"
            "chinesetraditional" -> "繁體中文"
            "english" -> "English"
            "esperanto" -> "Esperanto"
            "japanese" -> "日本語"
            else -> "unknown"
        }
    }
}