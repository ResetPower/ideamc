package org.imcl.color

import org.imcl.constraints.Toolkit
import org.imcl.constraints.logger
import java.util.*

object LeftListOpacityController {
    private val events = Vector<(int: Int) -> Unit>()
    @JvmStatic
    fun register(event: (int: Int) -> Unit) {
        logger.info("New event registered. Event@${events.size}")
        events.add(event)
    }
    @JvmStatic
    fun saveToConfig(int: Int) {
        logger.info("Saving left list opacity to config: $int")
        Toolkit.obj.getJSONObject("settings").put("leftListOpacity", int.toString())
        Toolkit.save()
    }
    @JvmStatic
    fun getFromConfig() : Int {
        val properties = Toolkit.obj.getJSONObject("settings")
        if (!properties.containsKey("leftListOpacity")) {
            saveToConfig(200)
            return 200
        }
        val ret = properties.getString("leftListOpacity")
        logger.info("Getting left list opacity from config: $ret")
        return Integer.parseInt(ret)
    }
    @JvmStatic
    fun updateLeftListOpacity(int: Int) {
        logger.info("Left list opacity updated, running all events")
        for (i in events) {
            i(if (int>255) 255 else int)
        }
    }
}