package org.imcl.main

import com.jfoenix.controls.JFXButton
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import javafx.stage.Stage
import org.imcl.lang.Translator
import org.imcl.launch.LauncherScene
import org.imcl.constraints.Toolkit
import org.imcl.users.OfflineUserInformation
import org.imcl.users.UserInformation
import org.imcl.users.YggdrasilUserInformation
import java.io.File


object MainScene {
    val emailLabel = Label("Email or username")
    val passwordLabel = Label("Password")
    val loginButton = JFXButton("Login")
    val offlineButton = JFXButton("Offline")
    @JvmStatic
    fun get(primaryStage: Stage) : Scene {
        val scene = Scene(BorderPane().apply {
            var translator = updateLanguage()
            background = Background(BackgroundImage(
                Image("file://"+ File("imcl/res/bg.png").absolutePath, 840.0, 502.5, false, true),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT
            ))
            val list = FXCollections.observableArrayList("English", "简体中文", "繁體中文", "Esperanto", "日本語")
            top = ChoiceBox(list).apply {
                selectionModel.select(Toolkit.getLanguageNameInThatLanguage(translator.languageName))
                selectionModel.selectedIndexProperty().addListener { observable, oldValue, newValue ->
                    Toolkit.updateLanguage(Toolkit.getLanguageEnglishName(list.get(newValue as Int)))
                    translator = Translator(Toolkit.getCurrentLanguage())
                    updateLanguage()
                }
                BorderPane.setMargin(this, Insets(20.0, 0.0, 100.0, 20.0))
            }
            center = GridPane().apply {
                opacity = 0.8
                background = Background(BackgroundFill(Color.WHITE, null, null))
                alignment = Pos.CENTER
                hgap = 10.0
                vgap = 10.0
                padding = Insets(25.0, 25.0, 25.0, 25.0)
                val scenetitle = Text("IMCL")
                scenetitle.font = Font.font("Tahoma", FontWeight.NORMAL, 20.0)
                add(scenetitle, 0, 0, 2, 1)
                add(emailLabel, 0, 1)
                val userTextField = TextField()
                add(userTextField, 1, 1)
                add(passwordLabel, 0, 2)
                val pwBox = PasswordField()
                add(pwBox, 1, 2)
                add(loginButton.apply {
                    setOnAction {
                        if (userTextField.text.trim()==""||pwBox.text.trim()=="") {
                            val alert = Alert(Alert.AlertType.INFORMATION)
                            alert.title = "Username or password not input"
                            alert.contentText = "Username or password not input"
                            alert.showAndWait()
                        } else {
                            primaryStage.scene = LauncherScene.get(translator, YggdrasilUserInformation(userTextField.text, pwBox.text))
                        }
                    }
                }, 0, 3)
                add(offlineButton.apply {
                    setOnAction {
                        if (userTextField.text.trim()=="") {
                            val alert = Alert(Alert.AlertType.INFORMATION)
                            alert.title = "Username not set"
                            alert.contentText = "Username not set"
                            alert.showAndWait()
                        } else {
                            primaryStage.scene = LauncherScene.get(translator, OfflineUserInformation(userTextField.text))
                        }
                    }
                }, 1, 3)
            }
            bottom = Label("").apply {
                BorderPane.setMargin(this, Insets(250.0, 0.0, 0.0, 0.0))
            }
            left = Label("").apply {
                BorderPane.setMargin(this, Insets(0.0, 250.0, 0.0, 0.0))
            }
            right = Label("").apply {
                BorderPane.setMargin(this, Insets(0.0, 0.0, 0.0, 250.0))
            }
        }, 840.0, 502.0)
        return scene
    }
    fun updateLanguage() : Translator {
        val translator = Translator(Toolkit.getCurrentLanguage())
        emailLabel.text = translator.get("email")
        passwordLabel.text = translator.get("password")
        loginButton.text = translator.get("login")
        offlineButton.text = translator.get("offline")
        return translator
    }
}