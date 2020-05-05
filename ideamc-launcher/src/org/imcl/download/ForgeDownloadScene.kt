package org.imcl.download

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDialog
import com.jfoenix.controls.JFXProgressBar
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.stage.Stage
import org.imcl.constraints.Toolkit
import org.imcl.core.http.HttpRequestSender
import org.imcl.lang.Translator
import java.awt.Desktop
import java.net.URI

object ForgeDownloadScene {
    @JvmStatic
    fun get(translator: Translator, primaryStage: Stage, sourceScene: Scene, launcherProfiles: JSONArray): Scene {
        var theScene = Scene(Label("Loading"), 840.0, 502.5)
        val stack = StackPane()
        stack.children.add(BorderPane().apply {
            top = HBox().apply {
                spacing = 5.0
                children.addAll(JFXButton("←").apply {
                    buttonType = JFXButton.ButtonType.RAISED
                    setOnAction {
                        primaryStage.scene = sourceScene
                    }
                }, Label("Forge ${translator.get("download")}").apply {
                    font = Font.font(20.0)
                })
            }
            center = ScrollPane().apply {
                content = VBox().apply {
                    val iterator = launcherProfiles.iterator()
                    while (iterator.hasNext()) {
                        val obj = iterator.next() as JSONObject
                        children.add(JFXButton(obj.getString("name")).apply {
                            setOnAction {
                                val ver = obj.getString("version")
                                if (ver.indexOf("-")!=-1) {
                                    Toolkit.toast(translator.get("thisversionisntpure"))
                                } else {
                                    // TODO Download Forge
                                    val progress = JFXDialog(stack, VBox().apply {
                                        children.addAll(Label("Loading"), JFXProgressBar())
                                    }, JFXDialog.DialogTransition.CENTER)
                                    progress.isOverlayClose = false
                                    progress.show()
                                    Thread {
                                        val str = HttpRequestSender.get("https://bmclapi2.bangbang93.com/forge/minecraft/$ver") {
                                            Platform.runLater {
                                                val alert = Alert(Alert.AlertType.ERROR)
                                                alert.contentText = "An error occurred in loading Forge list."
                                                alert.show()
                                            }
                                        }
                                        Platform.runLater {
                                            progress.close()
                                        }
                                        if (str.isEmpty()||str.trim()=="[]") {
                                            Platform.runLater {
                                                val alert = Alert(Alert.AlertType.ERROR)
                                                alert.title = "Error"
                                                alert.headerText = "Error"
                                                alert.contentText = "No available forge version for $ver."
                                                alert.show()
                                            }
                                        } else {
                                            Platform.runLater {
                                                primaryStage.scene = ForgeInstallerScene.get(translator, primaryStage, theScene, JSON.parseArray(str), ver)
                                            }
                                        }
                                    }.start()
                                }
                            }
                        })
                    }
                }
            }
            bottom = VBox().apply {
                children.addAll(Label(translator.get("bmclapi")), HBox().apply {
                    children.addAll(Label(translator.get("clickit")), Hyperlink("https://afdian.net/@bangbang93").apply {
                        setOnAction {
                            val desktop = Desktop.getDesktop()
                            if (Desktop.isDesktopSupported() && desktop.isSupported(Desktop.Action.BROWSE)) {
                                val uri = URI(text)
                                desktop.browse(uri)
                            }
                        }
                    })
                })
            }
        })
        theScene = Scene(stack, 840.0, 502.5)
        return theScene
    }
}