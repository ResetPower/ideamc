package org.imcl.main

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXComboBox
import com.jfoenix.controls.JFXPasswordField
import com.jfoenix.controls.JFXTextField
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import javafx.stage.Stage
import org.imcl.constraints.Toolkit
import org.imcl.constraints.logger
import org.imcl.core.authentication.YggdrasilAuthenticator
import org.imcl.core.network.NetworkState
import org.imcl.lang.Translator
import org.imcl.launch.LauncherScene
import org.imcl.users.OfflineUserInformation
import org.imcl.users.YggdrasilUserInformation

object MainScene {
    val emailLabel = Label("Email or username")
    val passwordLabel = Label("Password")
    val loginButton = JFXButton("Login")
    val offlineButton = JFXButton("Offline")
    @JvmStatic
    fun get(primaryStage: Stage) : Scene {
        logger.info("Initializing MainScene (login page)")
        val scene = Scene(BorderPane().apply {
            var translator = updateLanguage()
            logger.info("Getting language: ${translator.languageName}")
            background = Background(BackgroundImage(
                Image(MainScene::class.java.getResourceAsStream("/org/imcl/bg/bg.png"), 840.0, 502.5, false, true),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT
            ))
            val list = FXCollections.observableArrayList("English", "简体中文", "繁體中文", "Esperanto", "日本語")
            top = JFXComboBox(list).apply {
                selectionModel.select(Toolkit.getLanguageNameInThatLanguage(translator.languageName))
                selectionModel.selectedIndexProperty().addListener { observable, oldValue, newValue ->
                    logger.info("Updating language, old: ${Toolkit.getLanguageEnglishName(list.get(oldValue as Int))}, new: ${Toolkit.getLanguageEnglishName(list.get(newValue as Int))}")
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
                val userTextField = JFXTextField()
                add(userTextField, 1, 1)
                add(passwordLabel, 0, 2)
                val pwBox = JFXPasswordField()
                add(pwBox, 1, 2)
                add(loginButton.apply {
                    buttonType = JFXButton.ButtonType.RAISED
                    background = Background(BackgroundFill(Color.ALICEBLUE, null, null))
                    setOnAction {
                        logger.info("Login button clicked")
                        if (userTextField.text.trim()==""||pwBox.text.trim()=="") {
                            logger.info("Username or password not input")
                            val alert = Alert(Alert.AlertType.INFORMATION)
                            alert.title = translator.get("usernameorpasswordnotinput")
                            alert.contentText = translator.get("usernameorpasswordnotinput")
                            alert.showAndWait()
                        } else {
                            logger.info("Authenticating")
                            val result = YggdrasilAuthenticator.authenticate(userTextField.text, pwBox.text).split(" ")
                            if (result[0]=="true") {
                                logger.info("Authentication successful")
                                Toolkit.obj.getJSONObject("settings").put("isLoggedIn", "true")
                                val acinf = Toolkit.obj.getJSONObject("account")
                                val username = result[1]
                                val uuid = result[2]
                                val accessToken = result[3]
                                acinf.put("username", username)
                                acinf.put("uuid", uuid)
                                acinf.put("accessToken", accessToken)
                                acinf.put("email", userTextField.text)
                                Toolkit.save()
                                primaryStage.scene = LauncherScene.get(translator, YggdrasilUserInformation(username, uuid, accessToken, userTextField.text), primaryStage)
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
                        }
                    }
                }, 0, 3)
                add(offlineButton.apply {
                    buttonType = JFXButton.ButtonType.RAISED
                    background = Background(BackgroundFill(Color.ALICEBLUE, null, null))
                    setOnAction {
                        logger.info("Offline button clicked")
                        if (userTextField.text.trim()=="") {
                            logger.info("Username not input")
                            val alert = Alert(Alert.AlertType.INFORMATION)
                            alert.title = translator.get("usernamenotinput")
                            alert.contentText = translator.get("usernamenotinput")
                            alert.showAndWait()
                        } else {
                            logger.info("Logging in. Writing \"isLoggedIn: true\" to json")
                            Toolkit.obj.getJSONObject("settings").put("isLoggedIn", "true")
                            val acinf = Toolkit.obj.getJSONObject("account")
                            acinf.put("username", userTextField.text)
                            acinf.put("uuid", "none")
                            acinf.put("accessToken", "none")
                            Toolkit.save()
                            primaryStage.scene = LauncherScene.get(translator, OfflineUserInformation(userTextField.text), primaryStage)
                        }
                    }
                }, 1, 3)
            }
            bottom = Label("").apply {
                BorderPane.setMargin(this, Insets(250.0, 0.0, 0.0, 0.0))
            }
            left = Label("").apply {
                BorderPane.setMargin(this, Insets(0.0, 220.0, 0.0, 0.0))
            }
            right = Label("").apply {
                BorderPane.setMargin(this, Insets(0.0, 0.0, 0.0, 220.0))
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