package org.imcl.launch

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.jfoenix.controls.*
import javafx.geometry.HPos
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.control.Tab
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.stage.FileChooser
import javafx.stage.Stage
import org.imcl.constraints.Toolkit
import org.imcl.constraints.VERSION_CODE
import org.imcl.constraints.VERSION_NAME
import org.imcl.core.LaunchOptions
import org.imcl.core.Launcher
import org.imcl.core.authentication.OfflineAuthenticator
import org.imcl.core.authentication.YggdrasilAuthenticator
import org.imcl.core.http.HttpRequestSender
import org.imcl.download.MinecraftDownloadScene
import org.imcl.introductions.FolderSeparateIntroduction
import org.imcl.lang.Translator
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
        /*mainBorderPane.background = Background(
            BackgroundImage(
                Image("file://"+File("imcl/res/bg.png").absolutePath, 840.0, 502.5, false, true),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT
            )
        )*/
        val modsBorderPane = BorderPane()
        val profileList = JFXListView<Label>()
        var loadedProfiles = false
        val launcherProfiles = JSON.parseArray(File("imcl/launcher/launcher_profiles.json").readText())
        var theScene = Scene(Label("Loading..."), 840.0, 502.0)
        fun BorderPane.prepareModsBorderPane() {
            val selectedItem = profileList.selectionModel.selectedItem
            if (selectedItem==null) {
                center = Label("Profile not selected")
            } else {
                val selected = selectedItem.text
                val modList = JFXListView<Label>()
                val modsFolder = File((launcherProfiles[profileList.selectionModel.selectedIndex] as JSONObject).let {
                    if (it.getString("res-game-directory-separate")=="true") {
                        return@let it.getString("game-directory")+"/mods"
                    }
                    return@let it.getString("directory")+"/mods"
                })
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
                if (mods!=null) {
                    for (i in mods) {
                        modList.items.add(Label(i.name))
                    }
                }
                center = modList
                bottom = GridPane().apply {
                    add(Label("${translator.get("ver")}: $selected").apply {
                        background = Background(BackgroundFill(Color.WHITE, null, null))
                    }, 0, 0)
                    add(Label("${translator.get("modsfolder")}: ${modsFolder.path}").apply {
                        background = Background(BackgroundFill(Color.WHITE, null, null))
                    }, 0, 1)
                }
            }
        }
        fun setMinecraftJavaEditionPane() {
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
            val launchBtn = JFXButton(translator.get("launch")).apply {
                buttonType = JFXButton.ButtonType.RAISED
                background = Background(BackgroundFill(Color.LIGHTBLUE, null, null))
                setOnAction {
                    this.isDisable = true
                    val prof = launcherProfiles.getJSONObject(profileList.selectionModel.selectedIndex)
                    val autoConnectServer = prof.getString("auto-connect-server")
                    val autoConnectServerSplit = autoConnectServer.split(":")
                    val port = if (autoConnectServerSplit.size==1) {
                        "25565"
                    } else {
                        autoConnectServerSplit[1]
                    }
                    val width = prof.getString("width")
                    val height = prof.getString("height")
                    if (userInformation is OfflineUserInformation) {
                        Launcher.launch(LaunchOptions(prof.getString("directory"), prof.getString("version"), OfflineAuthenticator(userInformation.username()), Toolkit.getJavaPath(), jvmArgs = prof.getString("jvm-args"), minecraftArgs = "${if (width!="auto") "--width $width" else "" } ${if (height!="auto") "--height $height" else "" } ${if (prof.getString("auto-connect")=="true") "--server $autoConnectServer --port $port" else ""}", gameDirectory = if (prof.getString("game-directory")=="none") null else prof.getString("game-directory"))) {
                            this.isDisable = false
                        }
                    } else if (userInformation is YggdrasilUserInformation) {
                        Launcher.launch(LaunchOptions(prof.getString("directory"), prof.getString("version"), YggdrasilAuthenticator(userInformation.username(), userInformation.uuid(), userInformation.accessToken()), Toolkit.getJavaPath(), jvmArgs = prof.getString("jvm-args"), minecraftArgs = "${if (width!="auto") "--width $width" else "" } ${if (height!="auto") "--height $height" else "" } ${if (prof.getString("auto-connect")=="true") "--server $autoConnectServer --port $port" else ""}", gameDirectory = if (prof.getString("game-directory")=="none") null else prof.getString("game-directory"))) {
                            this.isDisable = false
                        }
                    }
                }
            }
            tabPane.tabs.addAll(Tab(translator.get("play")).apply {
                content = launchBtn
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
                                    launcherProfiles.add(JSONObject(mapOf(Pair("name", nm), Pair("version", verField.text), Pair("directory", dirField.text), Pair("width", "auto"), Pair("height", "auto"), Pair("jvm-args", ""), Pair("auto-connect", "false"), Pair("auto-connect-server", "true"), Pair("res-game-directory-separate", "false"), Pair("game-directory", "none"))))
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
                            addColumn( 0, Label(translator.get("edit")), Label(translator.get("name")), Label(translator.get("ver")), Label(translator.get("dir")), Label("Folder Separate"), Label("game-dir"))
                            val nameField = JFXTextField()
                            val verField = JFXTextField()
                            val dirField = JFXTextField()
                            val resGameDirectorySeparateBox = JFXCheckBox()
                            val gameDirField = JFXTextField()
                            nameField.text = theObj.getString("name")
                            verField.text = theObj.getString("version")
                            dirField.text = theObj.getString("directory")
                            resGameDirectorySeparateBox.isSelected = theObj.getString("res-game-directory-separate")=="true"
                            gameDirField.text = theObj.getString("game-directory")
                            add(nameField, 1, 1)
                            add(verField, 1, 2)
                            add(dirField, 1, 3)
                            add(resGameDirectorySeparateBox, 1, 4)
                            add(Hyperlink("What is this?").apply {
                                setOnAction {
                                    FolderSeparateIntroduction().show()
                                }
                            }, 2, 4)
                            add(gameDirField, 1, 5)
                            add(Label("If you don't want split folder, please keep it 'none'"), 1, 6)
                            add(JFXButton(translator.get("cancel")).apply {
                                setOnAction {
                                    secondStage.close()
                                }
                            }, 0, 7)
                            add(JFXButton(translator.get("edit")).apply {
                                setOnAction {
                                    theObj.set("name", nameField.text)
                                    theObj.set("version", verField.text)
                                    theObj.set("directory", dirField.text)
                                    theObj.set("res-game-directory-separate", resGameDirectorySeparateBox.isSelected.toString())
                                    theObj.set("game-directory", gameDirField.text)
                                    profileList.items[theIndex].text = nameField.text
                                    File("imcl/launcher/launcher_profiles.json").writeText(launcherProfiles.toJSONString())
                                    secondStage.close()
                                }
                            }, 1, 7)
                        }, 600.0, 251.0)
                        secondStage.show()
                    }
                }, JFXButton(translator.get("customizing")).apply {
                    buttonType = JFXButton.ButtonType.RAISED
                    background = Background(BackgroundFill(Color.LIGHTBLUE, null, null))
                    setOnAction {
                        val secondStage = Stage()
                        val theIndex = profileList.selectionModel.selectedIndex
                        val theObj = launcherProfiles[theIndex] as JSONObject
                        secondStage.scene = Scene(GridPane().apply {
                            addColumn( 0, Label(translator.get("customizing")), Label(translator.get("width")), Label(translator.get("height")), Label(translator.get("jvm-args")), Label(translator.get("auto-connect")), Label(translator.get("auto-connect-server")))
                            val widthField = JFXTextField()
                            val heightField = JFXTextField()
                            val jvmArgsField = JFXTextField()
                            val autoConnectBox = JFXCheckBox()
                            val autoConnectServerField = JFXTextField()
                            widthField.text = theObj.getString("width")
                            heightField.text = theObj.getString("height")
                            jvmArgsField.text = theObj.getString("jvm-args")
                            autoConnectBox.isSelected = theObj.getString("auto-connect")=="true"
                            autoConnectServerField.text = theObj.getString("auto-connect-server")
                            add(widthField, 1, 1)
                            add(heightField, 1, 2)
                            add(jvmArgsField, 1, 3)
                            add(autoConnectBox, 1, 4)
                            add(autoConnectServerField, 1, 5)
                            add(JFXButton(translator.get("cancel")).apply {
                                setOnAction {
                                    secondStage.close()
                                }
                            }, 0, 7)
                            add(JFXButton(translator.get("edit")).apply {
                                setOnAction {
                                    theObj.set("width", widthField.text)
                                    theObj.set("height", heightField.text)
                                    theObj.set("jvm-args", jvmArgsField.text)
                                    theObj.set("auto-connect", autoConnectBox.isSelected.toString())
                                    theObj.set("auto-connect-server", autoConnectServerField.text)
                                    File("imcl/launcher/launcher_profiles.json").writeText(launcherProfiles.toJSONString())
                                    secondStage.close()
                                }
                            }, 1, 7)
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
                    var con = true
                    val result = HttpRequestSender.get("https://sessionserver.mojang.com/session/minecraft/profile/${userInformation.uuid}") {
                        content = Label("It's look like something wrong. Please restart IMCL and retry.")
                        con = false
                    }
                    if (con) {
                        val str = JSON.parseObject(result).getJSONArray("properties").getJSONObject(0).getString("value")
                        val decodedObj = JSON.parseObject(String(Base64.getDecoder().decode(str)))
                        val url = decodedObj.getJSONObject("textures").getJSONObject("SKIN").getString("url")
                        content = BorderPane().apply {
                            top = HBox().apply {
                                children.addAll(JFXButton("Change Skin").apply {
                                    buttonType = JFXButton.ButtonType.RAISED
                                    background = Background(BackgroundFill(Color.LIGHTBLUE, null, null))
                                    setOnAction {
                                    }
                                }, JFXButton("Reset Skin").apply {
                                    buttonType = JFXButton.ButtonType.RAISED
                                    background = Background(BackgroundFill(Color.LIGHTBLUE, null, null))
                                    setOnAction {
                                    }
                                })
                            }
                            center = ImageView(url)
                        }
                    }
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
        val mainListView = VBox().apply {
            style = "-fx-background-color:#ffffff55"
            children.add(JFXButton(translator.get("news")).apply {
                setPrefSize(160.0, 20.0)
                setOnAction {
                    mainBorderPane.center = Label("News")
                }
            })
            children.add(JFXButton("Minecraft: Java Edition").apply {
                setPrefSize(160.0, 20.0)
                setOnAction {
                    setMinecraftJavaEditionPane()
                }
            })
            children.add(JFXButton(translator.get("settings")).apply {
                setPrefSize(160.0, 20.0)
                setOnAction {
                    mainBorderPane.center = GridPane().apply {
                        add(Label(if (userInformation is OfflineUserInformation) userInformation.username()+" - Offline" else if (userInformation is YggdrasilUserInformation) userInformation.username()+" - Yggdrasil" else "Unknown User" ).apply {
                            GridPane.setHalignment(this, HPos.CENTER)
                            font = Font.font(15.0)
                        }, 0, 0)
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
                        }, 1, 0)
                        add(Label("").apply {
                            GridPane.setHalignment(this, HPos.CENTER)
                        }, 0, 3)
                        val javaPathField = JFXTextField(Toolkit.getJavaPath())
                        add(Label("Java Path"), 0, 4)
                        add(javaPathField, 1, 4)
                        add(JFXButton("Save").apply {
                            buttonType = JFXButton.ButtonType.RAISED
                            background = Background(BackgroundFill(Color.LIGHTBLUE, null, null))
                            setOnAction {
                                Toolkit.setJavaPath(javaPathField.text)
                            }
                        }, 2, 4)
                    }
                }
            })
            children.add(JFXButton(translator.get("about")).apply {
                setPrefSize(160.0, 20.0)
                setOnAction {
                    mainBorderPane.center = Label("IDEA Minecraft Launcher\nDeveloper: ResetPower\nGitHub: https://github.com/resetpower/imcl\nVersion Name: $VERSION_NAME\nVersion Code: $VERSION_CODE\nOpen Source Software")
                }
            })
        }
        mainBorderPane.left = mainListView
        setMinecraftJavaEditionPane()
        theScene = Scene(AnchorPane().apply {
            children.add(ImageView(Image("file://"+File("imcl/res/bg.png").absolutePath, 840.0, 502.5, false, true)))
            children.add(mainBorderPane.apply {
                setPrefSize(840.0, 502.5)
            })
        }, 840.0, 502.0)
        return theScene
    }
}
