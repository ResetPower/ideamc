package org.imcl.download

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDialog
import com.jfoenix.controls.JFXProgressBar
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.stage.Stage
import org.imcl.constraints.Toolkit
import org.imcl.constraints.logger
import org.imcl.core.bmclapi.toBMCLAPIUrl
import org.imcl.core.download.DownloadManager
import org.imcl.core.ostool.OS
import org.imcl.core.ostool.OSTool
import org.imcl.lang.Translator
import java.io.File

object ForgeInstallerScene {
    @JvmStatic
    fun get(translator: Translator, primaryStage: Stage, sourceScene: Scene, forgeVersions: JSONArray, mcVer: String): Scene {
        val stack = StackPane()
        stack.children.add(BorderPane().apply {
            top = HBox().apply {
                spacing = 5.0
                children.addAll(JFXButton("←").apply {
                    buttonType = JFXButton.ButtonType.RAISED
                    setOnAction {
                        logger.info("Backing to ForgeDownloadScene")
                        primaryStage.scene = sourceScene
                    }
                }, Label("Forge ${translator.get("installer")}: $mcVer").apply {
                    font = Font.font(20.0)
                })
            }
            center = ScrollPane().apply {
                content = VBox().apply {
                    logger.info("Preparing Forge list")
                    val list = forgeVersions.toMutableList()
                    list.reverse()
                    val iterator = list.iterator()
                    while (iterator.hasNext()) {
                        val obj = iterator.next() as JSONObject
                        children.add(JFXButton(obj.getString("version")).apply {
                            setOnAction {
                                val progress = JFXDialog(stack, VBox().apply {
                                    children.addAll(Label(translator.get("downloading")), JFXProgressBar())
                                }, JFXDialog.DialogTransition.CENTER)
                                progress.show()
                                val mcVer = obj.getString("mcversion")
                                val ver = obj.getString("version")
                                Thread {
                                    logger.info("Downlaoding Installer Jar")
                                    if (GlobalDownloadSourceManager.downloadSrc=="bmclapi") {
                                        DownloadManager.download("https://files.minecraftforge.net/maven/net/minecraftforge/forge/$mcVer-$ver/forge-$mcVer-$ver-installer.jar".toBMCLAPIUrl(), File("cache/forge-$mcVer-$ver-installer.jar"))
                                    } else {
                                        DownloadManager.download("https://files.minecraftforge.net/maven/net/minecraftforge/forge/$mcVer-$ver/forge-$mcVer-$ver-installer.jar", File("cache/forge-$mcVer-$ver-installer.jar"))
                                    }
                                    Platform.runLater {
                                        progress.close()
                                    }
                                    logger.info("Downlaoding Done")
                                    logger.info("Launching Installer Jar")
                                    if (OSTool.getOS()==OS.Windows10||OSTool.getOS()==OS.Windows) {
                                        Runtime.getRuntime().exec(arrayOf("cmd.exe", "/C", "\"${Toolkit.getJavaPath()}\" -jar cache/forge-$mcVer-$ver-installer.jar"))
                                    } else {
                                        Runtime.getRuntime().exec(arrayOf("sh", "-c", "\"${Toolkit.getJavaPath()}\" -jar cache/forge-$mcVer-$ver-installer.jar"))
                                    }
                                }.start()
                            }
                        })
                    }
                    logger.info("Preparing Forge list done")
                }
            }
        })
        return Scene(stack, 840.0, 502.5)
    }
}