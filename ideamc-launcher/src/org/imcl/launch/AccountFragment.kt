package org.imcl.launch

import com.jfoenix.controls.JFXButton
import javafx.scene.control.Label
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.paint.Color
import org.imcl.constraints.Toolkit
import org.imcl.lang.Translator

object AccountFragment {
    @JvmStatic
    fun get(translator: Translator) = BorderPane().apply {
        val obj = Toolkit.obj
        if (!obj.containsKey("accounts")) {
            obj.set("accounts", emptyArray<Any>())
            Toolkit.save()
        }
        top = GridPane().apply {
            add(JFXButton(translator.get("add")).apply {
                buttonType = JFXButton.ButtonType.RAISED
                background = Background(BackgroundFill(Color.LIGHTBLUE, null, null))
                setOnAction {
                }
            }, 0, 0)
            add(JFXButton(translator.get("remove")).apply {
                buttonType = JFXButton.ButtonType.RAISED
                background = Background(BackgroundFill(Color.LIGHTBLUE, null, null))
                setOnAction {
                }
            }, 1, 0)
        }
        val accounts = obj.getJSONArray("accounts")
        if (accounts.isEmpty()) {
            center = Label(translator.get("noaccountsyet"))
        }
    }
}