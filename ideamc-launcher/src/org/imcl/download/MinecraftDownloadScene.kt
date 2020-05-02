package org.imcl.download

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXCheckBox
import com.jfoenix.controls.JFXListView
import com.jfoenix.controls.JFXScrollPane
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.layout.*
import javafx.scene.text.Font
import javafx.stage.Stage
import org.imcl.core.download.GameDownloader
import org.imcl.lang.Translator
import org.imcl.users.UserInformation
import java.awt.Desktop
import java.net.URI

object MinecraftDownloadScene {
    fun get(translator: Translator, userInformation: UserInformation, primaryStage: Stage, sourceScene: Scene, allVer: String) : Scene {
        val borderPane = BorderPane()
        val gameList = VBox()
        val versions = JSON.parseObject(allVer).getJSONArray("versions")
        var rel = true
        var sna = false
        var old = false
        var theScene = Scene(Label("Loading..."), 840.0, 502.0)
        fun refreshGameList() {
            val iterator = versions.iterator()
            gameList.children.clear()
            while (iterator.hasNext()) {
                val obj = (iterator.next() as JSONObject)
                val ver = obj.getString("id")
                if (obj.getString("type")=="release"&&rel) {
                    gameList.children.add(JFXButton(ver).apply {
                        setOnAction {
                            primaryStage.scene = MinecraftInstallerScene.get(translator, primaryStage, theScene, ver, obj)
                        }
                    })
                }
                if (obj.getString("type")=="snapshot"&&sna) {
                    gameList.children.add(JFXButton(obj.getString("id")).apply {
                        setOnAction {
                            primaryStage.scene = MinecraftInstallerScene.get(translator, primaryStage, theScene, ver, obj)
                        }
                    })
                }
                if ((obj.getString("type")=="old_alpha"||obj.getString("type")=="old_beta")&&old) {
                    gameList.children.add(JFXButton(obj.getString("id")).apply {
                        setOnAction {
                            primaryStage.scene = MinecraftInstallerScene.get(translator, primaryStage, theScene, ver, obj)
                        }
                    })
                }
            }
        }
        refreshGameList()
        val releaseBox = JFXCheckBox(translator.get("release")).apply {
            isSelected = true
            selectedProperty().addListener { _ ->
                rel = isSelected
                refreshGameList()
            }
        }
        val snapshotBox = JFXCheckBox(translator.get("snapshot")).apply {
            selectedProperty().addListener { _ ->
                sna = isSelected
                refreshGameList()
            }
        }
        val oldBox = JFXCheckBox(translator.get("old")).apply {
            selectedProperty().addListener { _ ->
                old = isSelected
                refreshGameList()
            }
        }
        borderPane.top = VBox().apply {
            children.addAll(HBox().apply {
                spacing = 5.0
                children.addAll(JFXButton("←").apply {
                    buttonType = JFXButton.ButtonType.RAISED
                    setOnAction {
                        primaryStage.scene = sourceScene
                    }
                }, Label("Minecraft ${translator.get("download")}").apply {
                    font = Font.font(20.0)
                })
            }, HBox().apply {
                padding = Insets(0.0, 0.0, 0.0, 10.0)
                spacing = 5.0
                children.addAll(releaseBox, snapshotBox, oldBox)
            })
        }
        borderPane.center = ScrollPane().apply {
            content = gameList
        }
        borderPane.bottom = VBox().apply {
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
        theScene = Scene(borderPane, 840.0, 502.0)
        return theScene
    }
}