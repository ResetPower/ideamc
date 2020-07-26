package org.imcl.updating

import javafx.scene.control.Alert
import org.imcl.constraints.VERSION_CODE
import org.imcl.constraints.logger
import org.imcl.core.network.NetworkState
import org.imcl.lang.Translator
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.Socket

object UpdateChecker {
    /** 检查IMCL版本是否最新
     * @return true表示当前版本为最新 false表示当前版本需要更新
     */
    @JvmStatic
    fun check(): Boolean {
        if (NetworkState.isConnectedToInternet()) {
            logger.info("Checking new version... IMCL version checking server is [service.kousaten.top:985]")
            val socket = Socket("service.kousaten.top", 985)
            val inputStreamReader = InputStreamReader(socket.getInputStream())
            val ins = BufferedReader(inputStreamReader)
            val text = ins.readLines()
            ins.close()
            logger.info("Checked new version.")
            for (i in text) {
                logger.info("TEXT: $i")
            }
            return Integer.parseInt(text[1]) == VERSION_CODE
        } else throw Exception("No network connect")
    }
    @JvmStatic
    fun showUpdater(translator: Translator) {
        Alert(Alert.AlertType.INFORMATION).run {
            headerText = translator.get("newversion")
            contentText = translator.get("newversionisavailable")
            show()
        }
    }
}
