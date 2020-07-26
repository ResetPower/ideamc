package org.imcl.constraints

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import javafx.scene.control.Alert
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

object Toolkit {
    lateinit var obj: JSONObject
    @JvmStatic
    fun init() {
        obj = JSON.parseObject(File("ideamc.json").readText())
    }
    @JvmStatic
    fun getCurrentLanguage() : String {
        return obj.getJSONObject("settings").getString("language")
    }
    @JvmStatic
    fun toast(message: String) {
        val alert = Alert(Alert.AlertType.INFORMATION)
        alert.title = "INFORMATION"
        alert.contentText = message
        alert.show()
    }
    @JvmStatic
    fun getJavaPath() : String {
        return obj.getJSONObject("settings").getString("javapath")
    }
    @JvmStatic
    fun setJavaPath(javaPath: String) {
        obj.getJSONObject("settings").put("javapath", javaPath)
        save()
    }
    @JvmStatic
    fun isLoggedIn() : Boolean {
        return obj.getJSONObject("settings").getString("isLoggedIn")=="true"
    }
    @JvmStatic
    fun updateLanguage(language: String) {
        obj.getJSONObject("settings").put("language", language)
        save()
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
    @JvmStatic
    fun getPluginsFolder() = File("plugins")
    @JvmStatic
    fun getHex(int: Int): String {
        val hex = Integer.toHexString(int)
        return if (hex.length==1) "0$hex" else hex
    }
    @JvmStatic
    fun save() {
        File("ideamc.json").writeText(obj.toJSONString())
    }
}