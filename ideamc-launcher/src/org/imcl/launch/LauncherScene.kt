package org.imcl.launch

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.jfoenix.controls.*
import javafx.collections.FXCollections
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.layout.*
import javafx.stage.Stage
import org.imcl.core.LaunchOptions
import org.imcl.core.Launcher
import org.imcl.core.authentication.OfflineAuthenticator
import org.imcl.lang.Translator
import org.imcl.users.UserInformation
import java.io.File
import java.util.*


object LauncherScene {
    @JvmStatic
    fun get(translator: Translator, userInformation: UserInformation) : Scene {
        val mainBorderPane = BorderPane()

        mainBorderPane.left = JFXListView<String>().apply {
            items.add("News")
            items.add("Minecraft: Java Edition")
            items.add("Kousaten: Java Edition")
            selectionModel.selectedItemProperty().addListener { observerable, oldValue, newValue ->
                when (newValue) {
                    "News" -> { mainBorderPane.center = Label("News") }
                    "Minecraft: Java Edition" -> {
                        val profileList = JFXListView<Label>()
                        val launcherProfiles = JSON.parseArray(File("imcl/launcher/launcher_profiles.json").readText())
                        val iterator = launcherProfiles.iterator()
                        while (iterator.hasNext()) {
                            val obj = JSON.toJSON(iterator.next()) as JSONObject
                            val nomo = obj.getString("name")
                            profileList.items.add(Label(nomo))
                        }
                        profileList.selectionModel.selectFirst()

                        val tabPane = JFXTabPane()
                        val gridPane1 = GridPane()
                        gridPane1.hgap = 10.0
                        gridPane1.vgap = 10.0
                        gridPane1.add(JFXButton(translator.get("launch")).apply {
                            setOnAction {
                                val prof = launcherProfiles.getJSONObject(profileList.selectionModel.selectedIndex)

                                Launcher.launch(LaunchOptions(prof.getString("directory"), prof.getString("version"), OfflineAuthenticator(userInformation.username)))
                            }
                        }, 2, 2)
                        gridPane1.background = Background(
                            BackgroundImage(
                                Image("file://"+File("imcl/res/bg.png").absolutePath, 840.0, 502.5, false, true),
                                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                                BackgroundSize.DEFAULT
                            )
                        )
                        tabPane.tabs.addAll(Tab("Play").apply {
                            content = gridPane1
                        }, Tab("Installations").apply {
                            val installations = GridPane()
                            installations.addRow(0, JFXButton(translator.get("add")).apply {
                                setOnAction {
                                    val secondStage = Stage()
                                    secondStage.scene = Scene(GridPane().apply {
                                        addColumn( 0, Label(translator.get("newprofile")), Label(translator.get("name")), Label(translator.get("ver")), Label(translator.get("dir")))
                                        val nameField = JFXTextField()
                                        val verField = JFXTextField()
                                        val dirField = JFXTextField()
                                        add(nameField, 1, 1)
                                        add(verField, 1, 2)
                                        add(dirField, 1, 3)
                                        add(JFXButton(translator.get("cancel")).apply {
                                            setOnAction {
                                                secondStage.close()
                                            }
                                        }, 0, 4)
                                        add(JFXButton(translator.get("add")).apply {
                                            setOnAction {
                                                val nm = nameField.text
                                                launcherProfiles.add(JSONObject(mapOf(Pair("name", nm), Pair("version", verField.text), Pair("directory", dirField.text))))
                                                File("imcl/launcher/launcher_profiles.json").writeText(launcherProfiles.toJSONString())
                                                profileList.items.add(Label(nm))
                                                secondStage.close()
                                            }
                                        }, 1, 4)
                                    }, 420.0, 251.0)
                                    secondStage.show()
                                }
                            }, JFXButton(translator.get("remove")).apply {
                                setOnAction {
                                    val theIndex = profileList.selectionModel.selectedIndex
                                    val theObj = launcherProfiles[theIndex]
                                    launcherProfiles.remove(theObj)
                                    File("imcl/launcher/launcher_profiles.json").writeText(launcherProfiles.toJSONString())
                                    profileList.items.removeAt(theIndex)
                                }
                            }, JFXButton(translator.get("edit")).apply {
                                setOnAction {

                                }
                            })
                            val instBorderPane = BorderPane()
                            instBorderPane.top = installations
                            instBorderPane.center = profileList
                            content = instBorderPane
                        }, Tab("Skin").apply {
                            content = Label("Skin")
                        })
                        mainBorderPane.center = tabPane
                    }
                    "Kousaten: Java Edition" -> { mainBorderPane.center = Label("Kousaten: Java Edition") }
                }
            }
            selectionModel.select("Minecraft: Java Edition")
        }

        return Scene(mainBorderPane, 840.0, 502.0)
    }
}