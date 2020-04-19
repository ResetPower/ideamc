package org.imcl.loading

import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.layout.*
import javafx.scene.text.Font
import javafx.stage.Stage
import java.io.File

object LoadingStage {
    fun get() : Stage {
        val stage = Stage()
        stage.scene = Scene(BorderPane().apply {
            background = Background(
                BackgroundImage(
                    Image("file://"+ File("imcl/res/bg.png").absolutePath, 420.0, 251.5, false, true),
                    BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                    BackgroundSize.DEFAULT
                )
            )
            center = Label("Loading...").apply {
                font = Font.font(50.0)
            }
        }, 420.0, 251.0)
        return stage
    }
}