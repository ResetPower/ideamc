package org.imcl.launch

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.jfoenix.controls.*
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.Label
import javafx.scene.control.Tab
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.stage.FileChooser
import javafx.stage.Stage
import org.imcl.bg.GlobalBackgroundImageController
import org.imcl.color.GlobalThemeColorController
import org.imcl.constraints.Toolkit
import org.imcl.constraints.VERSION_NAME
import org.imcl.constraints.logger
import org.imcl.core.LaunchOptions
import org.imcl.core.Launcher
import org.imcl.core.authentication.OfflineAuthenticator
import org.imcl.core.authentication.YggdrasilAuthenticator
import org.imcl.core.authentication.YggdrasilAuthenticator.Companion.refresh
import org.imcl.core.authentication.YggdrasilAuthenticator.Companion.validate
import org.imcl.core.download.GameDownloader
import org.imcl.core.http.HttpRequestSender
import org.imcl.core.network.NetworkState
import org.imcl.download.ForgeDownloadScene
import org.imcl.download.MinecraftDownloadScene
import org.imcl.installation.InstallationScene
import org.imcl.installation.InstallationSceneType
import org.imcl.lang.Translator
import org.imcl.main.MainScene
import org.imcl.users.OfflineUserInformation
import org.imcl.users.UserInformation
import org.imcl.users.YggdrasilUserInformation
import wan.dormsystem.utils.DialogBuilder
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.*
import kotlin.collections.isNotEmpty

object LauncherScene {
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
        var selected = 1
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
                background = Background(BackgroundFill(GlobalThemeColorController.getFromConfig(), CornerRadii(5.0), Insets(1.0)))
                setPrefSize(90.0, 35.0)
                setOnAction {
                    this.isDisable = true
                    if (userInformation is YggdrasilUserInformation) {
                        logger.info("Online account. Player name: " + userInformation.username)
                        if (validate(userInformation.accessToken)) {
                            logger.info("Validate access. Going to launch progress...")
                        } else {
                            logger.info("Validate not access, refreshing")
                            val newToken: String
                            try {
                                logger.info("Refreshing accessToken...")
                                newToken = refresh(userInformation.accessToken)
                                userInformation.accessToken = newToken
                                logger.info("Refresh successfully. Going to launch progress...")
                            } catch (e: java.lang.Exception) {
                                logger.info("Unable to refresh accessToken, opening re-logging in dialog...")
                                DialogBuilder(this).setTitle(translator.get("pleaseinputyourpassword")).setMessage("Email: ${userInformation.email}").setTextFieldText {
                                    val result = YggdrasilAuthenticator.authenticate(userInformation.email, it).split(" ")
                                    if (result[0]=="true") {
                                        logger.info("Authentication successful")
                                        Toolkit.obj.getJSONObject("settings").put("isLoggedIn", "true")
                                        val accessToken = result[3]
                                        userInformation.accessToken = accessToken
                                        Toolkit.obj.getJSONObject("account").put("accessToken", accessToken)
                                        Toolkit.save()
                                    } else {
                                        logger.info("Unable to authenticate")
                                        logger.info("Checking network state")
                                        if (NetworkState.isConnectedToInternet()) {
                                            logger.info("Network normal. Password error.")
                                            val alert = Alert(Alert.AlertType.INFORMATION)
                                            alert.title = translator.get("passworderror")
                                            alert.contentText = translator.get("passworderror")
                                            alert.show()
                                        } else {
                                            logger.info("Network bad. Network error.")
                                            val alert = Alert(Alert.AlertType.INFORMATION)
                                            alert.title = translator.get("networkerror")
                                            alert.contentText = translator.get("networkerror")
                                            alert.show()
                                        }
                                    }
                                }.setPositiveBtn(translator.get("login")).create()
                            }
                        }
                    }
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
                            if (userInformation is YggdrasilUserInformation) YggdrasilAuthenticator(userInformation.username(), userInformation.uuid, userInformation.accessToken) else OfflineAuthenticator(userInformation.username())
                            , Toolkit.getJavaPath(), jvmArgs = prof.getString("jvm-args"), minecraftArgs = "${if (width!="auto") "--width $width" else "" } ${if (height!="auto") "--height $height" else "" } ${if (prof.getString("auto-connect")=="true") "--server $autoConnectServer --port $port" else ""}",
                            gameDirectory = if (prof.getString("game-directory")=="none") null else prof.getString("game-directory"), loader = box, ver = VERSION_NAME), whenDone = whenDone, whenFinish = whenFinish)
                    } catch (e: Exception) {
                        launchProgress.close()
                        val errDial = JFXDialog(deepStackPane, Label("\n${translator.get("anerroroccurredinlaunching")}: \n${e.message}\n\n"), JFXDialog.DialogTransition.CENTER)
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
                        primaryStage.scene = InstallationScene.get(InstallationSceneType.NEW, translator, theScene, primaryStage, launcherProfiles = launcherProfiles, profileList = profileList)
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
                        val theIndex = profileList.selectionModel.selectedIndex
                        val theObj = launcherProfiles[theIndex] as JSONObject
                        primaryStage.scene = InstallationScene.get(InstallationSceneType.EDIT, translator, theScene, primaryStage, profileList = profileList, theIndex = theIndex, theObj = theObj)
                    }
                }, JFXButton(translator.get("customizing")).apply {
                    buttonType = JFXButton.ButtonType.RAISED
                    background = Background(BackgroundFill(Color.LIGHTBLUE, null, null))
                    setOnAction {
                        val theIndex = profileList.selectionModel.selectedIndex
                        val theObj = launcherProfiles[theIndex] as JSONObject
                        primaryStage.scene = InstallationScene.get(InstallationSceneType.CUSTOM, translator, theScene, primaryStage, profileList = profileList, theIndex = theIndex, theObj = theObj)
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
                        content = Label(translator.get("somethingwrong"))
                        con = false
                    }
                    if (con) {
                        val arr = JSON.parseObject(result).getJSONArray("properties")
                        val borderPane = BorderPane()
                        content = borderPane
                        borderPane.apply {
                            top = HBox().apply {
                                children.addAll(JFXButton(translator.get("resetskin")).apply {
                                    buttonType = JFXButton.ButtonType.RAISED
                                    background = Background(BackgroundFill(Color.LIGHTBLUE, null, null))
                                    setOnAction {
                                        DialogBuilder(this).setTitle("Tip").setMessage(translator.get("doyoureallywanttoresetskin")).setNegativeBtn("No").setPositiveBtn("Yes") {
                                            HttpRequestSender.delete("https://api.mojang.com/user/profile/${userInformation.uuid}/skin", head = Pair("Authorization", "Bearer ${userInformation.accessToken}")) {
                                                val alert = Alert(Alert.AlertType.ERROR)
                                                alert.contentText = translator.get("erroroccurredinresetingskin")
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
                            borderPane.center = Label(translator.get("noskinnow"))
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
                                children.addAll(Label("${translator.get("loading")}..."), JFXProgressBar())
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
        val accountBtn = JFXButton(translator.get("account"))
        val mcBtn = JFXButton("Minecraft")
        val setBtn = JFXButton(translator.get("settings"))
        val aboutBtn = JFXButton(translator.get("about"))
        val btnList = listOf(newsBtn, accountBtn, mcBtn, setBtn, aboutBtn)
        val selBg = Background(BackgroundFill(Color(0.6627451, 0.6627451, 0.6627451, 0.6), null, null))
        val unSelBg = Background(BackgroundFill(Color(0.0, 0.0, 0.0, 0.0), null, null))
        for (i in btnList) {
            i.minHeight = 50.0
        }
        fun changeSelected(s: Int) {
            selected = s
            for (i in btnList.indices) {
                if (i==selected) {
                    btnList[i].background = selBg
                } else {
                    btnList[i].background = unSelBg
                }
            }
        }
        val mainListView = VBox().apply {
            val color = GlobalThemeColorController.getFromConfig().toString().removePrefix("0x").removeSuffix("ff")
            logger.info("Generating main list view. ThemeColor: $color")
            style = "-fx-background-color:#${color}80"
            var ctext = color
            GlobalThemeColorController.register {
                val color = it.toString().removePrefix("0x").removeSuffix("ff")
                logger.info("Global theme color changed. New theme color: $color")
                style = "-fx-background-color:#${color}80"
                ctext = color
            }
            children.add(newsBtn.apply {
                isFocusTraversable = false
                setPrefSize(160.0, 20.0)
                font = Font.font(15.0)
                setOnAction {
                    logger.info("Turning to news page")
                    changeSelected(0)
                    mainBorderPane.center = NewsFragment.get(translator)
                }
            })
            children.add(accountBtn.apply {
                isFocusTraversable = false
                setPrefSize(160.0, 20.0)
                font = Font.font(15.0)
                setOnAction {
                    logger.info("Turning to account page")
                    changeSelected(1)
                    mainBorderPane.center = AccountFragment.get(translator)
                }
            })
            children.add(mcBtn.apply {
                isFocusTraversable = false
                setPrefSize(160.0, 20.0)
                font = Font.font(15.0)
                setOnAction {
                    logger.info("Turning to Minecraft page")
                    changeSelected(2)
                    setMinecraftJavaEditionPane()
                }
            })
            children.add(setBtn.apply {
                isFocusTraversable = false
                setPrefSize(160.0, 20.0)
                font = Font.font(15.0)
                setOnAction {
                    logger.info("Turning to settings page")
                    changeSelected(3)
                    mainBorderPane.center = SettingsFragment.get(translator, userInformation, primaryStage)
                }
            })
            children.add(aboutBtn.apply {
                isFocusTraversable = false
                setPrefSize(160.0, 20.0)
                font = Font.font(15.0)
                setOnAction {
                    logger.info("Turning to about page")
                    changeSelected(4)
                    mainBorderPane.center = AboutFragment.get(translator)
                }
            })
        }
        mainBorderPane.left = mainListView
        if (state==LaunchSceneState.SETTINGS) {
            logger.info("LauncherSceneState is SETTINGS, navigating to settings page")
            mainBorderPane.center = SettingsFragment.get(translator, userInformation, primaryStage)
            changeSelected(3)
        } else {
            setMinecraftJavaEditionPane()
            changeSelected(2)
        }
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
