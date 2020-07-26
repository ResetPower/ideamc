package org.imcl.installation

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXCheckBox
import com.jfoenix.controls.JFXListView
import com.jfoenix.controls.JFXTextField
import javafx.scene.Scene
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.text.Font
import javafx.stage.Stage
import org.imcl.constraints.Toolkit
import org.imcl.constraints.WINDOW_HEIGHT
import org.imcl.constraints.WINDOW_WIDTH
import org.imcl.constraints.logger
import org.imcl.introductions.FolderSeparateIntroduction
import org.imcl.lang.Translator

object InstallationScene {
    fun get(type: InstallationSceneType, translator: Translator, sourceScene: Scene, primaryStage: Stage,
            launcherProfiles: JSONArray? = null, profileList: JFXListView<Label>? = null, theObj: JSONObject? = null, theIndex: Int? = null): Scene {
        return Scene(BorderPane().apply {
            top = HBox().apply {
                spacing = 5.0
                children.addAll(JFXButton("←").apply {
                    buttonType = JFXButton.ButtonType.RAISED
                    setOnAction {
                        logger.info("Exit InstallationScene:$type")
                        primaryStage.scene = sourceScene
                    }
                }, Label(
                    when (type) {
                        InstallationSceneType.NEW -> translator.get("newinstallation")
                        InstallationSceneType.EDIT -> translator.get("editinstallation")
                        InstallationSceneType.CUSTOM -> translator.get("custominstallation")
                    }
                ).apply {
                    font = Font.font(20.0)
                })
            }
            if (type==InstallationSceneType.NEW) {
                center = GridPane().apply {
                    addColumn( 0, Label(""), Label(translator.get("name")), Label(translator.get("ver")), Label(translator.get("dir")))
                    val nameField = JFXTextField()
                    val verField = JFXTextField()
                    val dirField = JFXTextField()
                    add(nameField, 1, 1)
                    add(verField, 1, 2)
                    add(dirField, 1, 3)
                    add(JFXButton(translator.get("cancel")).apply {
                        setOnAction {
                            logger.info("Cancel clicked, exit InstallationScene:$type")
                            primaryStage.scene = sourceScene
                        }
                    }, 0, 5)
                    add(JFXButton(translator.get("add")).apply {
                        setOnAction {
                            val nm = nameField.text
                            launcherProfiles?.add(JSONObject(mapOf(Pair("name", nm), Pair("version", verField.text), Pair("directory", dirField.text), Pair("width", "auto"), Pair("height", "auto"), Pair("jvm-args", ""), Pair("auto-connect", "false"), Pair("auto-connect-server", "none"), Pair("res-game-directory-separate", "false"), Pair("game-directory", "none"))))
                            Toolkit.save()
                            profileList?.items?.add(Label(nm))
                            logger.info("OKed, exit InstallationScene:$type")
                            primaryStage.scene = sourceScene
                        }
                    }, 1, 5)
                }
            } else if (type==InstallationSceneType.EDIT) {
                center = GridPane().apply {
                    addColumn( 0, Label(""), Label(translator.get("name")), Label(translator.get("ver")), Label(translator.get("dir")), Label(translator.get("folderseparate")), Label(translator.get("gamedir")))
                    val nameField = JFXTextField()
                    val verField = JFXTextField()
                    val dirField = JFXTextField()
                    val resGameDirectorySeparateBox = JFXCheckBox()
                    val gameDirField = JFXTextField()
                    nameField.text = theObj?.getString("name")
                    verField.text = theObj?.getString("version")
                    dirField.text = theObj?.getString("directory")
                    resGameDirectorySeparateBox.isSelected = theObj?.getString("res-game-directory-separate")=="true"
                    gameDirField.text = theObj?.getString("game-directory")
                    add(nameField, 1, 1)
                    add(verField, 1, 2)
                    add(dirField, 1, 3)
                    add(resGameDirectorySeparateBox, 1, 4)
                    add(Hyperlink(translator.get("whatisthis")).apply {
                        setOnAction {
                            FolderSeparateIntroduction().show()
                        }
                    }, 2, 4)
                    add(gameDirField, 1, 5)
                    add(Label("If you don't want split folder, please keep it 'none'"), 1, 6)
                    add(JFXButton(translator.get("cancel")).apply {
                        setOnAction {
                            logger.info("Cancel clicked, exit InstallationScene:$type")
                            primaryStage.scene = sourceScene
                        }
                    }, 0, 7)
                    add(JFXButton(translator.get("edit")).apply {
                        setOnAction {
                            theObj?.set("name", nameField.text)
                            theObj?.set("version", verField.text)
                            theObj?.set("directory", dirField.text)
                            theObj?.set("res-game-directory-separate", resGameDirectorySeparateBox.isSelected.toString())
                            theObj?.set("game-directory", gameDirField.text)
                            profileList?.let {
                                if (theIndex!=null) {
                                    it.items[theIndex].text = nameField.text
                                }
                            }
                            Toolkit.save()
                            logger.info("OKed, exit InstallationScene:$type")
                            primaryStage.scene = sourceScene
                        }
                    }, 1, 7)
                }
            } else if (type==InstallationSceneType.CUSTOM) {
                center = GridPane().apply {
                    addColumn( 0, Label(""), Label(translator.get("width")), Label(translator.get("height")), Label(translator.get("jvm-args")), Label(translator.get("auto-connect")), Label(translator.get("auto-connect-server")))
                    val widthField = JFXTextField()
                    val heightField = JFXTextField()
                    val jvmArgsField = JFXTextField()
                    val autoConnectBox = JFXCheckBox()
                    val autoConnectServerField = JFXTextField()
                    widthField.text = theObj?.getString("width")
                    heightField.text = theObj?.getString("height")
                    jvmArgsField.text = theObj?.getString("jvm-args")
                    autoConnectBox.isSelected = theObj?.getString("auto-connect")=="true"
                    autoConnectServerField.text = theObj?.getString("auto-connect-server")
                    add(widthField, 1, 1)
                    add(heightField, 1, 2)
                    add(jvmArgsField, 1, 3)
                    add(autoConnectBox, 1, 4)
                    add(autoConnectServerField, 1, 5)
                    add(JFXButton(translator.get("cancel")).apply {
                        setOnAction {
                            logger.info("Cancel clicked, exit InstallationScene:$type")
                            primaryStage.scene = sourceScene
                        }
                    }, 0, 7)
                    add(JFXButton(translator.get("edit")).apply {
                        setOnAction {
                            theObj?.set("width", widthField.text)
                            theObj?.set("height", heightField.text)
                            theObj?.set("jvm-args", jvmArgsField.text)
                            theObj?.set("auto-connect", autoConnectBox.isSelected.toString())
                            theObj?.set("auto-connect-server", autoConnectServerField.text)
                            Toolkit.save()
                            logger.info("OKed, exit InstallationScene:$type")
                            primaryStage.scene = sourceScene
                        }
                    }, 1, 7)
                }
            }
        }, WINDOW_WIDTH, WINDOW_HEIGHT)
    }
}