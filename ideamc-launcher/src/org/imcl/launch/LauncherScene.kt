package org.imcl.launch

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.jfoenix.controls.*
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.stage.FileChooser
import javafx.stage.Stage
import org.imcl.bg.GlobalBackgroundImageController
import org.imcl.color.GlobalThemeColorController
import org.imcl.color.LeftListOpacityController
import org.imcl.constraints.Toolkit
import org.imcl.constraints.VERSION_NAME
import org.imcl.constraints.logger
import org.imcl.core.LaunchOptions
import org.imcl.core.Launcher
import org.imcl.core.authentication.OfflineAuthenticator
import org.imcl.core.authentication.YggdrasilAuthenticator
import org.imcl.core.download.GameDownloader
import org.imcl.core.http.HttpRequestSender
import org.imcl.download.ForgeDownloadScene
import org.imcl.download.MinecraftDownloadScene
import org.imcl.introductions.FolderSeparateIntroduction
import org.imcl.lang.Translator
import org.imcl.main.MainScene
import org.imcl.users.OfflineUserInformation
import org.imcl.users.UserInformation
import org.imcl.users.YggdrasilUserInformation
import org.imcl.platform.function.IMCLPage
import wan.dormsystem.utils.DialogBuilder
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.*

object LauncherScene {
    @JvmStatic
    val pages = Vector<Pair<String, IMCLPage>>()
    @JvmStatic
    fun get(translator: Translator, userInformation: UserInformation, primaryStage: Stage, state: LaunchSceneState = LaunchSceneState.DEFAULT) : Scene {
        logger.info("Initializing LauncherScene")
        val deepStackPane = StackPane()
        val mainBorderPane = BorderPane()
        val modsBorderPane = BorderPane()
        val profileList = JFXListView<Label>()
        var loadedProfiles = false
        val launcherProfiles = Toolkit.obj.getJSONArray("profiles")
        var theScene = Scene(Label("Loading..."), 840.0, 502.0)
        fun BorderPane.prepareModsBorderPane() {
            val selectedItem = profileList.selectionModel.selectedItem
            if (selectedItem==null) {
                center = Label(translator.get("profilenotselected"))
            } else {
                val selected = selectedItem.text
                val modList = JFXListView<Label>()
                val modsFolder = File((launcherProfiles[profileList.selectionModel.selectedIndex] as JSONObject).let {
                    if (it.getString("res-game-directory-separate")=="true") {
                        return@let it.getString("game-directory")+"/mods"
                    }
                    return@let it.getString("directory")+"/mods"
                })
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
                if (modsFolder.exists()) {
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
            tabPane.selectionModel.selectedIndexProperty().addListener { observable, oldValue, newValue ->
                logger.info("Changing MinecraftPane's tab to ${when (newValue) {
                    1 -> "Installations"
                    2 -> "Mods"
                    3 -> "Skin"
                    4 -> "Download"
                    else -> "Play"
                }
                }")
            }
            val launchBtn = JFXButton(translator.get("launch")).apply {
                buttonType = JFXButton.ButtonType.RAISED
                background = Background(BackgroundFill(Color.LIGHTGREEN, CornerRadii(5.0), Insets(1.0)))
                setPrefSize(90.0, 35.0)
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
                    val launchProgress = JFXDialog()
                    launchProgress.dialogContainer = deepStackPane
                    val box = VBox().apply {
                        children.addAll(VBox().apply {
                            children.addAll(Label(translator.get("launching")), JFXProgressBar())
                        })
                    }
                    launchProgress.content = box
                    launchProgress.isOverlayClose = false
                    launchProgress.show()
                    try {
                        val whenDone = {
                            this.isDisable = false
                            launchProgress.close()
                            primaryStage.isIconified = true
                        }
                        val whenFinish = {
                            Platform.runLater {
                                primaryStage.isIconified = false
                                primaryStage.show()
                            }
                        }
                        Launcher.launch(LaunchOptions(prof.getString("directory"), prof.getString("version"),
                            if (userInformation is YggdrasilUserInformation) YggdrasilAuthenticator(userInformation.username(), userInformation.uuid(), userInformation.accessToken()) else OfflineAuthenticator(userInformation.username())
                            , Toolkit.getJavaPath(), jvmArgs = prof.getString("jvm-args"), minecraftArgs = "${if (width!="auto") "--width $width" else "" } ${if (height!="auto") "--height $height" else "" } ${if (prof.getString("auto-connect")=="true") "--server $autoConnectServer --port $port" else ""}",
                            gameDirectory = if (prof.getString("game-directory")=="none") null else prof.getString("game-directory"), loader = box, ver = VERSION_NAME), whenDone = whenDone, whenFinish = whenFinish)
                    } catch (e: Exception) {
                        launchProgress.close()
                        val errDial = JFXDialog(deepStackPane, Label("\nAn error occurred in launching Minecraft: \n${e.message}\n\n"), JFXDialog.DialogTransition.CENTER)
                        errDial.show()
                        this.isDisable = false
                    }
                }
            }
            tabPane.tabs.addAll(Tab(translator.get("play")).apply {
                content = BorderPane().apply {
                    bottom = HBox().apply {
                        padding = Insets(10.0)
                        style = "-fx-background-color:darkgray"
                        opacity = 0.8
                        alignment = Pos.CENTER
                        spacing = 10.0
                        children.addAll(
                            launchBtn,
                            Label("").apply {
                                prefWidth = 200.0
                                textFill = Color.WHITE
                            },
                            Label(if (userInformation is OfflineUserInformation) userInformation.username else if (userInformation is YggdrasilUserInformation) userInformation.username else "Error")
                        )
                    }
                }
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
                                    launcherProfiles.add(JSONObject(mapOf(Pair("name", nm), Pair("version", verField.text), Pair("directory", dirField.text), Pair("width", "auto"), Pair("height", "auto"), Pair("jvm-args", ""), Pair("auto-connect", "false"), Pair("auto-connect-server", "none"), Pair("res-game-directory-separate", "false"), Pair("game-directory", "none"))))
                                    Toolkit.save()
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
                        profileList.items.removeAt(theIndex)
                        Toolkit.save()
                    }
                }, JFXButton(translator.get("edit")).apply {
                    buttonType = JFXButton.ButtonType.RAISED
                    background = Background(BackgroundFill(Color.LIGHTBLUE, null, null))
                    setOnAction {
                        val secondStage = Stage()
                        val theIndex = profileList.selectionModel.selectedIndex
                        val theObj = launcherProfiles[theIndex] as JSONObject
                        secondStage.scene = Scene(GridPane().apply {
                            addColumn( 0, Label(translator.get("edit")), Label(translator.get("name")), Label(translator.get("ver")), Label(translator.get("dir")), Label(translator.get("folderseparate")), Label(translator.get("gamedir")))
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
                            add(Hyperlink(translator.get("whatisthis")).apply {
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
                                    Toolkit.save()
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
                                    Toolkit.save()
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
                        val arr = JSON.parseObject(result).getJSONArray("properties")
                        val borderPane = BorderPane()
                        content = borderPane
                        borderPane.apply {
                            top = HBox().apply {
                                children.addAll(JFXButton("Reset Skin").apply {
                                    buttonType = JFXButton.ButtonType.RAISED
                                    background = Background(BackgroundFill(Color.LIGHTBLUE, null, null))
                                    setOnAction {
                                        DialogBuilder(this).setTitle("Tip").setMessage("Do you really want to reset you skin?").setNegativeBtn("No").setPositiveBtn("Yes") {
                                            HttpRequestSender.delete("https://api.mojang.com/user/profile/${userInformation.uuid}/skin", head = Pair("Authorization", "Bearer ${userInformation.accessToken}")) {
                                                val alert = Alert(Alert.AlertType.ERROR)
                                                alert.contentText = "Error occurred in Reseting Skin."
                                                alert.show()
                                            }
                                        }.create()
                                    }
                                })
                            }
                        }
                        if (arr.isNotEmpty()) {
                            val str = arr.getJSONObject(0).getString("value")
                            val decodedObj = JSON.parseObject(String(Base64.getDecoder().decode(str)))
                            val url = decodedObj.getJSONObject("textures").getJSONObject("SKIN").getString("url")
                            borderPane.center = ImageView(url)
                        } else {
                            borderPane.center = Label("No skin now.")
                        }
                    }
                } else {
                    content = Label(translator.get("offlinemodenotsupport")).apply {
                        background = Background(BackgroundFill(Color.WHITE, null, null))
                    }
                }
            }, Tab(translator.get("download")).apply {
                content = VBox().apply {
                    alignment = Pos.CENTER
                    spacing = 10.0
                    children.add(JFXButton("Minecraft").apply {
                        buttonType = JFXButton.ButtonType.RAISED
                        background = Background(BackgroundFill(Color.LIGHTGREEN, null, null))
                        setOnAction {
                            logger.info("Turning to MinecraftDownloadScene")
                            val progress = JFXDialog(deepStackPane, VBox().apply {
                                children.addAll(Label("Loading..."), JFXProgressBar())
                            }, JFXDialog.DialogTransition.CENTER)
                            progress.isOverlayClose = false
                            progress.show()
                            Thread {
                                val allVer = GameDownloader.getAllVersions()
                                Platform.runLater {
                                    progress.close()
                                    primaryStage.scene = MinecraftDownloadScene.get(translator, userInformation, primaryStage, theScene, allVer)
                                }
                            }.start()
                        }
                    })
                    children.add(JFXButton("Forge").apply {
                        buttonType = JFXButton.ButtonType.RAISED
                        background = Background(BackgroundFill(Color.DARKGRAY, null, null))
                        setOnAction {
                            logger.info("Turning to ForgeDownloadScene")
                            primaryStage.scene = ForgeDownloadScene.get(translator, primaryStage, theScene, launcherProfiles)
                        }
                    })
                    children.add(JFXButton("Fabric").apply {
                        buttonType = JFXButton.ButtonType.RAISED
                        background = Background(BackgroundFill(Color.LIGHTCYAN, null, null))
                        setOnAction {
                            logger.info("Fabric downloading not supported, no actions")
                            // TODO Download Fabric
                        }
                    })
                    children.add(JFXButton("Optifine").apply {
                        buttonType = JFXButton.ButtonType.RAISED
                        background = Background(BackgroundFill(Color.NAVAJOWHITE, null, null))
                        setOnAction {
                            logger.info("Optifine downloading not supported, no actions")
                            // TODO Download Optifine
                        }
                    })
                }
            })
            mainBorderPane.center = tabPane
        }
        val newsBtn = JFXButton(translator.get("news"))
        val mcBtn = JFXButton("Minecraft")
        val setBtn = JFXButton(translator.get("settings"))
        val aboutBtn = JFXButton(translator.get("about"))
        val selBg = Background(BackgroundFill(Color(0.1, 0.1, 0.1, 0.5), null, null))
        val mainListView = VBox().apply {
            val color = GlobalThemeColorController.getFromConfig().toString().removePrefix("0x").removeSuffix("ff")
            val opacity = Toolkit.getHex(LeftListOpacityController.getFromConfig())
            logger.info("Generating main list view. ThemeColor: $color, Opacity: $opacity")
            style = "-fx-background-color:#$color$opacity"
            var ctext = color
            GlobalThemeColorController.register {
                val color = it.toString().removePrefix("0x").removeSuffix("ff")
                val opacity = Toolkit.getHex(LeftListOpacityController.getFromConfig())
                logger.info("Global theme color changed. New theme color: $color")
                style = "-fx-background-color:#$color$opacity"
                ctext = color
            }
            LeftListOpacityController.register {
                val opacity = Toolkit.getHex(it)
                style = "-fx-background-color:#$ctext$opacity"
                logger.info("Left list opacity changed. New opacity: $opacity")
            }
            children.add(Label(""))
            children.add(newsBtn.apply {
                isFocusTraversable = false
                setPrefSize(160.0, 20.0)
                font = Font.font(15.0)
                setOnAction {
                    logger.info("Turning to news page")
                    mainBorderPane.center = NewsFragment.get(translator)
                }
            })
            children.add(Label(""))
            children.add(mcBtn.apply {
                isFocusTraversable = false
                setPrefSize(160.0, 20.0)
                font = Font.font(15.0)
                setOnAction {
                    logger.info("Turning to Minecraft page")
                    setMinecraftJavaEditionPane()
                    logger.info("Turned to Minecraft page")
                }
            })
            children.add(Label(""))
            children.add(setBtn.apply {
                isFocusTraversable = false
                setPrefSize(160.0, 20.0)
                font = Font.font(15.0)
                setOnAction {
                    logger.info("Turning to settings page")
                    mainBorderPane.center = SettingsFragment.get(translator, userInformation, primaryStage)
                    logger.info("Turned to settings page")
                }
            })
            children.add(Label(""))
            children.add(aboutBtn.apply {
                isFocusTraversable = false
                setPrefSize(160.0, 20.0)
                font = Font.font(15.0)
                setOnAction {
                    logger.info("Turning to about page")
                    mainBorderPane.center = AboutFragment.get(translator)
                    logger.info("Turned to about page")
                }
            })
            logger.info("Finding plugins' pages")
            for (i in pages) {
                logger.info("Adding page ${i.first}")
                children.add(Label(""))
                children.add(JFXButton(i.first).apply {
                    isFocusTraversable = false
                    setPrefSize(160.0, 20.0)
                    font = Font.font(15.0)
                    setOnAction {
                        logger.info("Turning to ${i.first} page")
                        mainBorderPane.center = i.second.node
                        logger.info("Turned to ${i.first} page")
                    }
                })
            }
        }
        mainBorderPane.left = mainListView
        if (state==LaunchSceneState.SETTINGS) {
            logger.info("LauncherSceneState is SETTINGS, navigating to settings page")
            mainBorderPane.center = SettingsFragment.get(translator, userInformation, primaryStage)
        } else setMinecraftJavaEditionPane()
        theScene = Scene(deepStackPane.apply {
            children.add(AnchorPane().apply {
                val bg = GlobalBackgroundImageController.getFromConfig()
                children.add(ImageView(Image(if (bg=="") MainScene::class.java.getResourceAsStream("/org/imcl/bg/bg.png") else FileInputStream(bg), 840.0, 502.5, false, true)))
                children.add(mainBorderPane.apply {
                    setPrefSize(840.0, 502.5)
                })
                GlobalBackgroundImageController.register {
                    children[0] = ImageView(Image(if (it=="") MainScene::class.java.getResourceAsStream("/org/imcl/bg/bg.png") else FileInputStream(it), 840.0, 502.5, false, true))
                }
            })
        }, 840.0, 502.0)
        return theScene
    }
}
