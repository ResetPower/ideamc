package org.imcl.platform.lang

import org.imcl.constraints.Toolkit

object LangTool {
    @JvmStatic
    fun getLang(): Language {
        return when(Toolkit.getCurrentLanguage()) {
            "english" -> Language.ENGLISH
            "simplifiedchinese" -> Language.SIMPLIFIED_CHINESE
            "traditionalchinese" -> Language.TRADITIONAL_CHINESE
            "japanese" -> Language.JAPANESE
            "esperanto" -> Language.ESPERANTO
            else -> Language.ENGLISH
        }
    }
}