package org.imcl.core.infowindow

import javafx.scene.Scene
import javafx.stage.Stage
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane

class InfoWindow : Stage() {
    val theLabel = Label("")
    init {
        scene = Scene(ScrollPane(theLabel), 840.0, 251.0)
    }
}