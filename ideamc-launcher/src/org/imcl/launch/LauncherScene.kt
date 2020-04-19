package org.imcl.launch

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.jfoenix.controls.*
import javafx.geometry.HPos
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.Tab
import javafx.scene.image.Image
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import javafx.stage.Stage
import org.imcl.constraints.Toolkit
import org.imcl.constraints.VERSION_CODE
import org.imcl.constraints.VERSION_NAME
import org.imcl.core.LaunchOptions
import org.imcl.core.Launcher
import org.imcl.core.authentication.OfflineAuthenticator
import org.imcl.core.authentication.YggdrasilAuthenticator
import org.imcl.download.MinecraftDownloadScene
import org.imcl.lang.Translator
import org.imcl.loading.LoadingStage
import org.imcl.main.MainScene
import org.imcl.users.OfflineUserInformation
import org.imcl.users.UserInformation
import org.imcl.users.YggdrasilUserInformation
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.*

object LauncherScene {
    @JvmStatic
    fun get(translator: Translator, userInformation: UserInformation, primaryStage: Stage) : Scene {
        val mainBorderPane = BorderPane()
        val modsBorderPane = BorderPane()
        val profileList = JFXListView<Label>()
        var loadedProfiles = false
        val launcherProfiles = JSON.parseArray(File("imcl/launcher/launcher_profiles.json").readText())
        var theScene = Scene(Label("Loading..."), 840.0, 502.0)
        fun BorderPane.prepareModsBorderPane() {
            val selected = profileList.selectionModel.selectedItem.text
            val modList = JFXListView<Label>()
            val modsFolder = File((launcherProfiles[profileList.selectionModel.selectedIndex] as JSONObject).getString("directory")+"/mods")
            if (!modsFolder.exists()) {
                modsFolder.mkdirs()
            }
            top = GridPane().apply {
                add(JFXButton(translator.get("remove")).apply {
                    buttonType = JFXButton.ButtonType.RAISED
                    background = Background(BackgroundFill(Color.LIGHTBLUE, null, null))
                    setOnAction {
                        val theIndex = modList.selectionModel.selectedIndex
                        val i = File("${modsFolder.path}/${modList.items[theIndex].text}")
                        modList.items.removeAt(theIndex)
                        if (i.exists()) {
                            i.delete()
                        }
                    }
                }, 0, 2)
                add(JFXButton(translator.get("add")).apply {
                    buttonType = JFXButton.ButtonType.RAISED
                    background = Background(BackgroundFill(Color.LIGHTBLUE, null, null))
                    setOnAction {
                        val fc = FileChooser()
                        fc.selectedExtensionFilter = FileChooser.ExtensionFilter("Jar File (*.jar)", ".jar")
                        val result = fc.showOpenDialog(primaryStage)
                        if (result!=null) {
                            val nomo = result.name
                            modList.items.add(Label(nomo))
                            Files.copy(result.toPath(), FileOutputStream("${modsFolder.path}/$nomo"))
                        }
                    }
                }, 1, 2)
                add(JFXButton(translator.get("refresh")).apply {
                    buttonType = JFXButton.ButtonType.RAISED
                    background = Background(BackgroundFill(Color.LIGHTBLUE, null, null))
                    setOnAction {
                        prepareModsBorderPane()
                    }
                }, 2, 2)
                add(JFXButton(translator.get("enable")).apply {
                    buttonType = JFXButton.ButtonType.RAISED
                    background = Background(BackgroundFill(Color.LIGHTBLUE, null, null))
                    setOnAction {
                        val theIndex = modList.selectionModel.selectedIndex
                        val i = File("${modsFolder.path}/${modList.items[theIndex].text}")
                        if (i.name.toLowerCase().endsWith(".jar")) {
                            Toolkit.toast("This mod has been enabled.")
                        } else {
                            i.renameTo(File("${modsFolder.path}/${modList.items[theIndex].text.removeSuffix(".disable")}"))
                            modList.items[theIndex].text = modList.items[theIndex].text.removeSuffix(".disable")
                        }
                    }
                }, 3, 2)
                add(JFXButton(translator.get("disable")).apply {
                    buttonType = JFXButton.ButtonType.RAISED
                    background = Background(BackgroundFill(Color.LIGHTBLUE, null, null))
                    setOnAction {
                        val theIndex = modList.selectionModel.selectedIndex
                        val i = File("${modsFolder.path}/${modList.items[theIndex].text}")
                        if (i.name.toLowerCase().endsWith(".jar.disable")) {
                            Toolkit.toast("This mod has been disabled.")
                        } else {
                            i.renameTo(File("${modsFolder.path}/${modList.items[theIndex].text}.disable"))
                            modList.items[theIndex].text = modList.items[theIndex].text+".disable"
                        }
                    }
                }, 4, 2)
            }
            val mods = modsFolder.listFiles { dir, name ->
                if (name.toLowerCase().endsWith(".jar")||name.toLowerCase().endsWith(".jar.disable")) {
                    val mod = File("${dir.path}/$name")
                    return@listFiles !mod.isDirectory
                }
                return@listFiles false
            }
            for (i in mods) {
                modList.items.add(Label(i.name))
            }
            center = modList
            bottom = GridPane().apply {
                add(Label("${translator.get("ver")}: $selected"), 0, 0)
                add(Label("${translator.get("modsfolder")}: ${modsFolder.path}"), 0, 1)
            }
        }
        mainBorderPane.left = JFXListView<String>().apply {
            styleClass.add("mylistview")
            items.add(translator.get("news"))
            items.add("Minecraft: Java Edition")
            items.add(translator.get("settings"))
            items.add(translator.get("about"))
            border = null
            selectionModel.selectedItemProperty().addListener { observerable, oldValue, newValue ->
                when (newValue) {
                    translator.get("news") -> { mainBorderPane.center = Label("News") }
                    "Minecraft: Java Edition" -> {
                        if (!loadedProfiles) {
                            val iterator = launcherProfiles.iterator()
                            while (iterator.hasNext()) {
                                val obj = JSON.toJSON(iterator.next()) as JSONObject
                                val nomo = obj.getString("name")
                                profileList.items.add(Label(nomo))
                            }
                            profileList.selectionModel.selectFirst()
                            loadedProfiles = true
                        }

                        val tabPane = JFXTabPane()
                        val gridPane1 = GridPane()
                        gridPane1.hgap = 10.0
                        gridPane1.vgap = 10.0
                        gridPane1.add(JFXButton(translator.get("launch")).apply {
                            buttonType = JFXButton.ButtonType.RAISED
                            background = Background(BackgroundFill(Color.LIGHTBLUE, null, null))
                            setOnAction {
                                this.isDisable = true
                                val prof = launcherProfiles.getJSONObject(profileList.selectionModel.selectedIndex)
                                if (userInformation is OfflineUserInformation) {
                                    Launcher.launch(LaunchOptions(prof.getString("directory"), prof.getString("version"), OfflineAuthenticator(userInformation.username()))) {
                                        this.isDisable = false
                                    }
                                } else if (userInformation is YggdrasilUserInformation) {
                                    Launcher.launch(LaunchOptions(prof.getString("directory"), prof.getString("version"), YggdrasilAuthenticator(userInformation.username(), userInformation.uuid(), userInformation.accessToken()))) {
                                        this.isDisable = false
                                    }
                                }
                            }
                        }, 2, 2)
                        gridPane1.background = Background(
                            BackgroundImage(
                                Image("file://"+File("imcl/res/bg.png").absolutePath, 840.0, 502.5, false, true),
                                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                                BackgroundSize.DEFAULT
                            )
                        )
                        tabPane.tabs.addAll(Tab(translator.get("play")).apply {
                            content = gridPane1
                        }, Tab(translator.get("installations")).apply {
                            val installations = GridPane()
                            installations.addRow(0, JFXButton(translator.get("add")).apply {
                                buttonType = JFXButton.ButtonType.RAISED
                                background = Background(BackgroundFill(Color.LIGHTBLUE, null, null))
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
                                        }, 0, 5)
                                        add(JFXButton(translator.get("add")).apply {
                                            setOnAction {
                                                val nm = nameField.text
                                                launcherProfiles.add(JSONObject(mapOf(Pair("name", nm), Pair("version", verField.text), Pair("directory", dirField.text))))
                                                File("imcl/launcher/launcher_profiles.json").writeText(launcherProfiles.toJSONString())
                                                profileList.items.add(Label(nm))
                                                secondStage.close()
                                            }
                                        }, 1, 5)
                                    }, 420.0, 251.0)
                                    secondStage.show()
                                }
                            }, JFXButton(translator.get("remove")).apply {
                                buttonType = JFXButton.ButtonType.RAISED
                                background = Background(BackgroundFill(Color.LIGHTBLUE, null, null))
                                setOnAction {
                                    val theIndex = profileList.selectionModel.selectedIndex
                                    val theObj = launcherProfiles[theIndex]
                                    launcherProfiles.remove(theObj)
                                    File("imcl/launcher/launcher_profiles.json").writeText(launcherProfiles.toJSONString())
                                    profileList.items.removeAt(theIndex)
                                }
                            }, JFXButton(translator.get("edit")).apply {
                                buttonType = JFXButton.ButtonType.RAISED
                                background = Background(BackgroundFill(Color.LIGHTBLUE, null, null))
                                setOnAction {
                                    val secondStage = Stage()
                                    val theIndex = profileList.selectionModel.selectedIndex
                                    val theObj = launcherProfiles[theIndex] as JSONObject
                                    secondStage.scene = Scene(GridPane().apply {
                                        addColumn( 0, Label(translator.get("edit")), Label(translator.get("name")), Label(translator.get("ver")), Label(translator.get("dir")))
                                        val nameField = JFXTextField()
                                        val verField = JFXTextField()
                                        val dirField = JFXTextField()
                                        nameField.text = theObj.getString("name")
                                        verField.text = theObj.getString("version")
                                        dirField.text = theObj.getString("directory")
                                        add(nameField, 1, 1)
                                        add(verField, 1, 2)
                                        add(dirField, 1, 3)
                                        add(JFXButton(translator.get("cancel")).apply {
                                            setOnAction {
                                                secondStage.close()
                                            }
                                        }, 0, 5)
                                        add(JFXButton(translator.get("edit")).apply {
                                            setOnAction {
                                                val nm = nameField.text
                                                theObj.set("name", nameField.text)
                                                theObj.set("versions", verField.text)
                                                theObj.set("directory", dirField.text)
                                                profileList.items[theIndex].text = nameField.text
                                                File("imcl/launcher/launcher_profiles.json").writeText(launcherProfiles.toJSONString())
                                                secondStage.close()
                                            }
                                        }, 1, 5)
                                    }, 420.0, 251.0)
                                    secondStage.show()
                                }
                            })
                            profileList.selectionModel.selectedItemProperty().addListener { observable, oldValue, newValue ->
                                modsBorderPane.apply {
                                    prepareModsBorderPane()
                                }
                            }
                            val instBorderPane = BorderPane()
                            instBorderPane.top = installations
                            instBorderPane.center = profileList
                            content = instBorderPane
                        }, Tab(translator.get("mods")).apply {
                            content = modsBorderPane.apply {
                                prepareModsBorderPane()
                            }
                        }, Tab(translator.get("skin")).apply {
                            if (userInformation is YggdrasilUserInformation) {
                                content = Label("This feature is not supported now.")
                            } else {
                                content = Label("Offline mode not support Skin. Please buy Minecraft.")
                            }
                        }, Tab(translator.get("download")).apply {
                            content = VBox().apply {
                                alignment = Pos.CENTER
                                spacing = 10.0
                                children.add(JFXButton("Minecraft").apply {
                                    buttonType = JFXButton.ButtonType.RAISED
                                    background = Background(BackgroundFill(Color.LIGHTGREEN, null, null))
                                    setOnAction {
                                        primaryStage.scene = MinecraftDownloadScene.get(translator, userInformation, primaryStage, theScene)
                                    }
                                })
                                children.add(JFXButton("Forge").apply {
                                    buttonType = JFXButton.ButtonType.RAISED
                                    background = Background(BackgroundFill(Color.DARKGRAY, null, null))
                                    setOnAction {
                                        // TODO Download Forge
                                    }
                                })
                                children.add(JFXButton("Optifine").apply {
                                    buttonType = JFXButton.ButtonType.RAISED
                                    background = Background(BackgroundFill(Color.NAVAJOWHITE, null, null))
                                    setOnAction {
                                        // TODO Download Optifine
                                    }
                                })
                                children.add(JFXButton("Fabric").apply {
                                    buttonType = JFXButton.ButtonType.RAISED
                                    background = Background(BackgroundFill(Color.LIGHTCYAN, null, null))
                                    setOnAction {
                                        // TODO Download Fabric
                                    }
                                })
                            }
                        })
                        mainBorderPane.center = tabPane
                    }
                    translator.get("settings") -> { mainBorderPane.center = GridPane().apply {
                        add(Label(if (userInformation is OfflineUserInformation) userInformation.username()+" - Offline" else if (userInformation is YggdrasilUserInformation) userInformation.username()+" - Yggdrasil" else "Unknown User" ).apply {
                            GridPane.setHalignment(this, HPos.CENTER)
                        }, 0, 0)
                        add(Label("").apply {
                            GridPane.setHalignment(this, HPos.CENTER)
                        }, 0, 1)
                        add(JFXButton(translator.get("logout")).apply {
                            buttonType = JFXButton.ButtonType.RAISED
                            background = Background(BackgroundFill(Color.LIGHTBLUE, null, null))
                            setOnAction {
                                primaryStage.scene = MainScene.get(primaryStage)
                                val ins = FileInputStream("imcl/properties/ideamc.properties")
                                val prop = Properties()
                                prop.load(ins)
                                ins.close()
                                prop.setProperty("isLoggedIn", "false")
                                val out = FileOutputStream("imcl/properties/ideamc.properties")
                                prop.store(out, "")
                                out.close()
                            }
                            GridPane.setHalignment(this, HPos.CENTER)
                        }, 0, 2)
                    } }
                    translator.get("about") -> {
                        mainBorderPane.center = Label("IDEA Minecraft Launcher\nDeveloper: ResetPower\nGitHub: https://github.com/resetpower/imcl\nVersion Name: $VERSION_NAME\nVersion Code: $VERSION_CODE\nOpen Source Software")
                    }
                }
            }
            selectionModel.select("Minecraft: Java Edition")
        }
        theScene = Scene(mainBorderPane, 840.0, 502.0)
        return theScene
    }
}
