package org.imcl.download

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.jfoenix.controls.JFXButton
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.Label
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import org.imcl.constraints.logger
import org.imcl.core.bmclapi.toBMCLAPIUrl
import org.imcl.core.download.DownloadManager
import org.imcl.core.ostool.OS
import org.imcl.core.ostool.OSTool
import org.imcl.lang.Translator
import java.io.File

object MinecraftInstallerScene {
    @JvmStatic
    fun get(translator: Translator, primaryStage: Stage, sourceScene: Scene, version: String, jsonObject: JSONObject) : Scene {
        val os = OSTool.getOS()
        val un = System.getProperty("user.name")
        val dir = Label(if (os==OS.MacOS) "/Users/$un/Library/Application Support/minecraft"
                        else if (os==OS.Windows||os==OS.Windows10) "C:\\Users\\$un\\AppData\\Roaming\\.minecraft"
                        else if (os==OS.Linux) "/home/$un/minecraft" else "/minecraft" )
        logger.info("Generating MinecraftInstallerScene, OS=$os, username=$un, defaultInstallDir=$dir")
        val borderPane = BorderPane()
        borderPane.top = VBox().apply {
            children.addAll(HBox().apply {
                spacing = 5.0
                children.addAll(JFXButton("←").apply {
                    buttonType = JFXButton.ButtonType.RAISED
                    setOnAction {
                        logger.info("Backing to MinecraftDownloadScene")
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
                        logger.info("Selecting install path, opening DirectoryChooser")
                        val fc = DirectoryChooser()
                        val f = fc.showDialog(primaryStage)
                        if (f!=null) {
                            logger.info("File chose, file.path=${f.path}")
                            dir.text = f.path
                        } else {
                            logger.info("Chose file is null, user closed DirectoryChooser or clicked cancel")
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
                    logger.info("Installing Minecraft $version")
                    try {
                        val bmclapi = (GlobalDownloadSourceManager.downloadSrc=="bmclapi")
                        logger.info("Download source is ${if (bmclapi) "BMCLAPI" else "Official"}")
                        Platform.runLater {
                            theLabel.text = "Downloading JSON ..."
                        }
                        logger.info("Downloading JSON")
                        val jsonFile = File("$fol/versions/$version/$version.json")
                        DownloadManager.download(jsonObject.getString("url").apply {
                            if (bmclapi) toBMCLAPIUrl() else this
                        }, jsonFile) {
                            DownloadManager.download(jsonObject.getString("url"), jsonFile)
                        }
                        logger.info("Downloading Asset Index")
                        val obj = JSON.parseObject(jsonFile.readText())
                        Platform.runLater {
                            theLabel.text = "Downloading Asset Index ..."
                        }
                        val assetIndexObj = obj.getJSONObject("assetIndex")
                        val asset = assetIndexObj.getString("id")
                        val assetFile = File("$fol/assets/indexes/$asset.json")
                        DownloadManager.download(assetIndexObj.getString("url").run {
                            if (bmclapi) toBMCLAPIUrl() else this
                        }, assetFile) {
                            DownloadManager.download(assetIndexObj.getString("url"), assetFile)
                        }
                        logger.info("Downloading Client jar")
                        Platform.runLater {
                            theLabel.text = "Downloading Client ..."
                        }
                        val downloadsClientObj = obj.getJSONObject("downloads").getJSONObject("client")
                        val jarFile = File("$fol/versions/$version/$version.jar")
                        DownloadManager.download(downloadsClientObj.getString("url").run {
                            if (bmclapi) toBMCLAPIUrl() else this
                        }, jarFile) {
                            DownloadManager.download(assetIndexObj.getString("url"), jarFile)
                        }
                        logger.info("Downloading assets file")
                        Platform.runLater {
                            theLabel.text = "Downloading Assets ..."
                        }
                        val assetIndex = JSON.parseObject(File("$fol/assets/indexes/$asset.json").readText()).getJSONObject("objects")
                        val iterator = assetIndex.keys.iterator()
                        while (iterator.hasNext()) {
                            val obj = assetIndex.getJSONObject(iterator.next())
                            val hash = obj.getString("hash")
                            val theAsset = File("$fol/assets/objects/${hash.substring(0, 2)}/$hash")
                            logger.info("Downloading assets/objects/${hash.substring(0, 2)}/$hash")
                            Platform.runLater {
                                theLabel.text = "Downloading assets/objects/${hash.substring(0, 2)}/$hash"
                            }
                            DownloadManager.download("https://resources.download.minecraft.net/${hash.substring(0, 2)}/$hash".run {
                                if (bmclapi) toBMCLAPIUrl() else this
                            }, theAsset) {
                                DownloadManager.download(assetIndexObj.getString("url"), theAsset)
                            }
                        }
                        Platform.runLater {
                            theLabel.text = "Downloaded Successful"
                            primaryStage.scene = sourceScene
                        }
                        logger.info("Download Successful, backing to MinecraftDownloadScene")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Platform.runLater {
                            logger.error("Error occurred in downloading")
                            val a = Alert(Alert.AlertType.ERROR)
                            a.title = "Error occurred in downloading."
                            a.headerText = "Error occurred in downloading."
                            a.contentText = e.localizedMessage
                            a.show()
                        }
                    }
                }.start()
            }
        }
        return Scene(borderPane, 840.0, 502.0)
    }
}