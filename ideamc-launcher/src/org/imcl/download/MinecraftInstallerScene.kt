package org.imcl.download

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXProgressBar
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import org.imcl.core.bmclapi.toBMCLAPIUrl
import org.imcl.core.download.DownloadManager
import org.imcl.lang.Translator
import java.io.File

object MinecraftInstallerScene {
    @JvmStatic
    fun get(translator: Translator, primaryStage: Stage, sourceScene: Scene, version: String, jsonObject: JSONObject) : Scene {
        val dir = Label("/Users/${System.getProperty("user.name")}/Library/Application Support/minecraft")
        val borderPane = BorderPane()
        borderPane.top = VBox().apply {
            children.addAll(HBox().apply {
                spacing = 5.0
                children.addAll(JFXButton("←").apply {
                    buttonType = JFXButton.ButtonType.RAISED
                    setOnAction {
                        primaryStage.scene = sourceScene
                    }
                }, Label("Minecraft ${translator.get("installer")}: $version").apply {
                    font = Font.font(20.0)
                })
            }, HBox().apply {
                padding = Insets(0.0, 0.0, 0.0, 10.0)
                spacing = 5.0
                children.addAll(Label(translator.get("installpath")), Label("     "), dir, JFXButton(translator.get("browse")).apply {
                    buttonType = JFXButton.ButtonType.RAISED
                    background = Background(BackgroundFill(Color.NAVAJOWHITE, null, null))
                    setOnAction {
                        val fc = DirectoryChooser()
                        val f = fc.showDialog(primaryStage)
                        if (f!=null) {
                            dir.text = f.path
                        }
                    }
                })
            })
        }
        val theLabel = Label()
        borderPane.bottom = theLabel
        borderPane.center = JFXButton(translator.get("install")).apply {
            buttonType = JFXButton.ButtonType.RAISED
            background = Background(BackgroundFill(Color.AQUA, null, null))
            setOnAction {
                this.isDisable = true
                val fol = dir.text
                Thread {
                    Platform.runLater {
                        theLabel.text = "Downloading JSON ..."
                    }
                    val jsonFile = File("$fol/versions/$version/$version.json")
                    DownloadManager.download(jsonObject.getString("url").toBMCLAPIUrl(), jsonFile)
                    val obj = JSON.parseObject(jsonFile.readText())
                    Platform.runLater {
                        theLabel.text = "Downloading Asset Index ..."
                    }
                    val assetIndexObj = obj.getJSONObject("assetIndex")
                    DownloadManager.download(assetIndexObj.getString("url").toBMCLAPIUrl(), File("$fol/assets/indexes/${assetIndexObj.getString("id")}.json"))
                    Platform.runLater {
                        theLabel.text = "Downloading Client ..."
                    }
                    val downloadsClientObj = obj.getJSONObject("downloads").getJSONObject("client")
                    DownloadManager.download(downloadsClientObj.getString("url").toBMCLAPIUrl(), File("$fol/versions/$version/$version.jar"))
                    Platform.runLater {
                        theLabel.text = "Downloaded Successful"
                    }
                }.start()
            }
        }
        return Scene(borderPane, 840.0, 502.0)
    }
}