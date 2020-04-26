package org.imcl.lang

class Translator(val languageName: String) {
    private val map: LanguageMap
    init {
        val factory = LanguageMapFactory.newInstance()
        val lang = when (languageName) {
            "english" -> Language.ENGLISH
            "esperanto" -> Language.ESPERANTO
            "japanese" -> Language.JAPANESE
            "chinesesimplified" -> Language.CHINESE_SIMPLIFIED
            "chinesetraditional" -> Language.CHINESE_TRADITIONAL
            else -> Language.ENGLISH
        }
        map = factory.newLanguageMap(lang)
    }
    fun get(key: String) = map.get(key)
}