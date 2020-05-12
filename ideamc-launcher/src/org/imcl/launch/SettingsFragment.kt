package org.imcl.launch

import com.jfoenix.controls.*
import javafx.collections.FXCollections
import javafx.geometry.HPos
import javafx.scene.control.Label
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.GridPane
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.stage.Stage
import org.imcl.bg.GlobalBackgroundImageController
import org.imcl.color.GlobalThemeColorController
import org.imcl.color.LeftListOpacityController
import org.imcl.constraints.Toolkit
import org.imcl.constraints.logger
import org.imcl.download.GlobalDownloadSourceManager
import org.imcl.main.MainScene
import org.imcl.users.OfflineUserInformation
import org.imcl.users.UserInformation
import org.imcl.users.YggdrasilUserInformation
import org.imcl.lang.Translator

object SettingsFragment {
    fun get(translator: Translator, userInformation: UserInformation, primaryStage: Stage) = GridPane().apply {
        logger.info("Generating SettingsFragment")
        add(Label(if (userInformation is OfflineUserInformation) userInformation.username()+" - Offline" else if (userInformation is YggdrasilUserInformation) userInformation.username()+" - Yggdrasil" else "Unknown User" ).apply {
            GridPane.setHalignment(this, HPos.CENTER)
            font = Font.font(15.0)
        }, 0, 0)
        add(JFXButton(translator.get("logout")).apply {
            buttonType = JFXButton.ButtonType.RAISED
            background = Background(BackgroundFill(Color.LIGHTBLUE, null, null))
            setOnAction {
                logger.info("Logging out")
                primaryStage.scene = MainScene.get(primaryStage)
                Toolkit.obj.getJSONObject("settings")["isLoggedIn"] = "false"
                Toolkit.save()
            }
            GridPane.setHalignment(this, HPos.CENTER)
        }, 1, 0)
        add(Label("").apply {
            GridPane.setHalignment(this, HPos.CENTER)
        }, 0, 3)
        val javaPathField = JFXTextField(Toolkit.getJavaPath())
        add(Label("Java Path"), 0, 4)
        add(javaPathField, 1, 4)
        add(JFXButton(translator.get("save")).apply {
            buttonType = JFXButton.ButtonType.RAISED
            background = Background(BackgroundFill(Color.LIGHTBLUE, null, null))
            setOnAction {
                val javaPath = javaPathField.text
                logger.info("Save javapath to $javaPath")
                Toolkit.setJavaPath(javaPath)
            }
        }, 2, 4)
        add(Label(""), 0, 5)
        add(Label(translator.get("themecolor")), 0, 6)
        add(JFXColorPicker(GlobalThemeColorController.getFromConfig()).apply {
            valueProperty().addListener { _ ->
                val valu = value
                logger.info("Set themecolor to $valu")
                GlobalThemeColorController.saveToConfig(valu)
                GlobalThemeColorController.updateThemeColor(valu)
            }
        }, 1, 6)
        add(Label(""), 0, 7)
        add(Label(translator.get("lefttabopacity")), 0, 8)
        add(JFXSlider().apply {
            value = LeftListOpacityController.getFromConfig()/2.5
            valueProperty().addListener { _ ->
                val valu = value
                logger.info("Set left list opacity to $valu")
                LeftListOpacityController.saveToConfig((valu*2.6).toInt())
                LeftListOpacityController.updateLeftListOpacity((valu*2.6).toInt())
            }
        }, 1, 8)
        add(Label(translator.get("numberhighandopacityhigh")), 0, 9)
        add(Label(""), 0, 10)
        add(Label(translator.get("backgroundimage")), 0, 11)
        val bgIField = JFXTextField().apply {
            text = GlobalBackgroundImageController.getFromConfig()
        }
        add(bgIField, 1, 11)
        add(JFXButton(translator.get("save")).apply {
            buttonType = JFXButton.ButtonType.RAISED
            background = Background(BackgroundFill(Color.LIGHTBLUE, null, null))
            setOnAction {
                logger.info("Set background imabe to ${bgIField.text}")
                GlobalBackgroundImageController.saveToConfig(bgIField.text)
                GlobalBackgroundImageController.updateBackgroundImage(bgIField.text)
            }
        }, 2, 11)
        add(Label(translator.get("usedefaultpleasekeepnone")), 0, 12)
        add(Label(""), 0, 13)
        add(Label(translator.get("downloadsrc")), 0, 14)
        add(JFXComboBox<String>().apply {
            items.add(translator.get("official"))
            items.add("bmclapi")
            selectionModel.select(when (GlobalDownloadSourceManager.getFromConfig()) {
                "official" -> 0
                "bmclapi" -> 1
                else -> 0
            })
            selectionModel.selectedIndexProperty().addListener { observerable, oldValue, newValue ->
                val str = when (newValue) {
                    0 -> "official"
                    1 -> "bmclapi"
                    else -> "official"
                }
                logger.info("Save download source to $str")
                GlobalDownloadSourceManager.saveToConfig(str)
            }
        }, 1, 14)
        add(Label(""), 0, 15)
        add(Label(translator.get("language")), 0, 16)
        val list = FXCollections.observableArrayList("English", "简体中文", "繁體中文", "Esperanto", "日本語")
        add(JFXComboBox(list).apply {
            selectionModel.select(Toolkit.getLanguageNameInThatLanguage(translator.languageName))
            selectionModel.selectedIndexProperty().addListener { observable, oldValue, newValue ->
                logger.info("Language updated from ${list.get(oldValue as Int)} to ${list.get(newValue as Int)}")
                Toolkit.updateLanguage(Toolkit.getLanguageEnglishName(list.get(newValue as Int)))
                primaryStage.scene = LauncherScene.get(Translator(Toolkit.getCurrentLanguage()), userInformation, primaryStage, state = LaunchSceneState.SETTINGS)
            }
        }, 1, 16)
    }
}