package org.imcl.bg

import org.imcl.constraints.Toolkit
import org.imcl.constraints.logger
import java.util.*

object GlobalBackgroundImageController {
    private val events = Vector<(string: String) -> Unit>()
    @JvmStatic
    fun register(event: (string: String) -> Unit) {
        logger.info("New event registered. Event@${events.size}")
        events.add(event)
    }
    @JvmStatic
    fun saveToConfig(string: String) {
        logger.info("Saving background image to config: $string")
        Toolkit.obj.getJSONObject("settings").put("backgroundImage", string)
        Toolkit.save()
    }
    @JvmStatic
    fun getFromConfig() : String {
        val properties = Toolkit.obj.getJSONObject("settings")
        if (!properties.containsKey("backgroundImage")) {
            saveToConfig("")
            return ""
        }
        val ret = properties.getString("backgroundImage")
        logger.info("Getting background image from config: $ret")
        return ret
    }
    @JvmStatic
    fun updateBackgroundImage(string: String) {
        logger.info("Background image updated, running all events")
        for (i in events) {
            i(string)
        }
    }
}