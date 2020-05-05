package org.imcl.bg

import javafx.scene.paint.Color
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

object GlobalBackgroundImageController {
    private val events = Vector<(string: String) -> Unit>()
    @JvmStatic
    fun register(event: (string: String) -> Unit) {
        events.add(event)
    }
    @JvmStatic
    fun saveToConfig(string: String) {
        val properties = Properties()
        val `in` = FileInputStream("imcl/properties/ideamc.properties")
        properties.load(`in`)
        `in`.close()
        properties.setProperty("backgroundImage", string)
        val out = FileOutputStream("imcl/properties/ideamc.properties")
        properties.store(out, "")
        out.close()
    }
    @JvmStatic
    fun getFromConfig() : String {
        val properties = Properties()
        val `in` = FileInputStream("imcl/properties/ideamc.properties")
        properties.load(`in`)
        `in`.close()
        if (!properties.containsKey("backgroundImage")) {
            saveToConfig("")
            return ""
        }
        return properties.getProperty("backgroundImage")
    }
    @JvmStatic
    fun updateBackgroundImage(string: String) {
        for (i in events) {
            i(string)
        }
    }
}