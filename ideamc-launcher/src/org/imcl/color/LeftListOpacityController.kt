package org.imcl.color

import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

object LeftListOpacityController {
    private val events = Vector<(int: Int) -> Unit>()
    @JvmStatic
    fun register(event: (int: Int) -> Unit) {
        events.add(event)
    }
    @JvmStatic
    fun saveToConfig(int: Int) {
        val properties = Properties()
        val `in` = FileInputStream("imcl/properties/ideamc.properties")
        properties.load(`in`)
        `in`.close()
        properties.setProperty("leftListOpacity", (if (int>255) 255 else int).toString())
        val out = FileOutputStream("imcl/properties/ideamc.properties")
        properties.store(out, "")
        out.close()
    }
    @JvmStatic
    fun getFromConfig() : Int {
        val properties = Properties()
        val `in` = FileInputStream("imcl/properties/ideamc.properties")
        properties.load(`in`)
        `in`.close()
        if (!properties.containsKey("leftListOpacity")) {
            saveToConfig(200)
            return 200
        }
        return Integer.parseInt(properties.getProperty("leftListOpacity"))
    }
    @JvmStatic
    fun updateLeftListOpacity(int: Int) {
        for (i in events) {
            i(if (int>255) 255 else int)
        }
    }
}