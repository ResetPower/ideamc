package org.imcl.color

import javafx.scene.paint.Color
import org.imcl.constraints.Toolkit
import org.imcl.constraints.logger
import java.util.*

object GlobalThemeColorController {
    private val events = Vector<(color: Color) -> Unit>()
    @JvmStatic
    fun register(event: (color: Color) -> Unit) {
        logger.info("New event registered. Event@${events.size}")
        events.add(event)
    }
    @JvmStatic
    fun saveToConfig(color: Color) {
        logger.info("Saving theme color to config: $color")
        Toolkit.obj.getJSONObject("settings").put("themeColor", color.toString())
        Toolkit.save()
    }
    @JvmStatic
    fun getFromConfig() : Color {
        val properties = Toolkit.obj.getJSONObject("settings")
        if (!properties.containsKey("themeColor")) {
            saveToConfig(Color.WHITE)
            return Color.WHITE
        }
        val ret = properties.getString("themeColor")
        logger.info("Saving theme color to config: $ret")
        return Color.web(ret)
    }
    @JvmStatic
    fun updateThemeColor(color: Color) {
        logger.info("Theme color updated, running all events")
        for (i in events) {
            i(color)
        }
    }
}