package org.imcl.color

import javafx.scene.paint.Color
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

object GlobalThemeColorController {
    private val events = Vector<(color: Color) -> Unit>()
    @JvmStatic
    fun register(event: (color: Color) -> Unit) {
        events.add(event)
    }
    @JvmStatic
    fun saveToConfig(color: Color) {
        val properties = Properties()
        val `in` = FileInputStream("imcl/properties/ideamc.properties")
        properties.load(`in`)
        `in`.close()
        properties.setProperty("themeColor", color.toString())
        val out = FileOutputStream("imcl/properties/ideamc.properties")
        properties.store(out, "")
        out.close()
    }
    @JvmStatic
    fun getFromConfig() : Color {
        val properties = Properties()
        val `in` = FileInputStream("imcl/properties/ideamc.properties")
        properties.load(`in`)
        `in`.close()
        if (!properties.containsKey("themeColor")) {
            saveToConfig(Color.WHITE)
            return Color.WHITE
        }
        return Color.web(properties.getProperty("themeColor"))
    }
    @JvmStatic
    fun updateThemeColor(color: Color) {
        for (i in events) {
            i(color)
        }
    }
}